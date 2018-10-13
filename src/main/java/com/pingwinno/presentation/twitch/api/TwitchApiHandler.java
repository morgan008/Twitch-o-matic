package com.pingwinno.presentation.twitch.api;


import com.pingwinno.application.StorageHelper;
import com.pingwinno.application.twitch.playlist.handler.UserIdGetter;
import com.pingwinno.application.twitch.playlist.handler.VodMetadataHelper;
import com.pingwinno.domain.VodDownloader;
import com.pingwinno.infrastructure.SettingsProperties;
import com.pingwinno.infrastructure.models.NotificationDataModel;
import com.pingwinno.infrastructure.models.StreamExtendedDataModel;
import com.pingwinno.infrastructure.models.StreamStatusNotificationModel;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;


@Path("/handler")
public class TwitchApiHandler {
    private org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    private String lastNotificationId;


    @GET
    public Response getSubscriptionQuery(@Context UriInfo info) {
        Response response = null;
        log.debug("Incoming challenge request");
        if (info != null) {
            String hubMode = info.getQueryParameters().getFirst("hub.mode");
            //handle denied response
            if (hubMode.equals("denied")) {
                String hubReason = info.getQueryParameters().getFirst("hub.reason");
                response = Response.status(Response.Status.OK).build();
                log.warn("Subscription failed. Reason:{}", hubReason);
                return response;
            }
            //handle verify response
            else {
                String hubChallenge = info.getQueryParameters().getFirst("hub.challenge");
                response = Response.status(Response.Status.OK).entity(hubChallenge).build();
                log.debug("Subscription complete {} hub.challenge is:{}", hubMode, hubChallenge);
                log.info(" Twith-o-matic started. Waiting for stream up");
            }
        } else log.warn("Subscription query is not correct. Try restart Twitch-o-matic.");

        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleStreamNotification(StreamStatusNotificationModel dataModel) throws IOException, InterruptedException {
        log.debug("Incoming stream up/down notification");
        NotificationDataModel[] notificationArray = dataModel.getData();
        if (notificationArray.length > 0) {
            log.info("Stream is up");
            NotificationDataModel notificationModel = notificationArray[0];
            //check for notification duplicate
            if ((!(notificationModel.getId().equals(lastNotificationId))) &&
                    //filter for live streams
                    (notificationModel.getType().equals("live")) &&
                    (notificationModel.getUser_id().equals(UserIdGetter.getUserId(SettingsProperties.getUser())))) {
                lastNotificationId = notificationModel.getId();

                StreamExtendedDataModel streamMetadata = VodMetadataHelper.getLastVod(SettingsProperties.getUser());
                streamMetadata.setUuid(StorageHelper.getUuidName());

                log.info("Try to start record");
                VodDownloader vodDownloader = new VodDownloader();

                if (streamMetadata.getVodId() != null) {

                    new Thread(() -> vodDownloader.initializeDownload(streamMetadata)).start();

                    String startedAt = notificationModel.getStarted_at();
                    log.info("Record started at: {}", startedAt);
                } else {
                    log.error("vodId is null. Stream not found");
                }

            }
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }
}

