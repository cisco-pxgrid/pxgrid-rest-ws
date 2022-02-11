package com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory;

import java.time.OffsetDateTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.SampleConfiguration;
import com.cisco.pxgrid.samples.ise.StompFrame;

public class LossDetectionSessionHandler extends SesssionHandlerImpl{
	private static Logger logger = LoggerFactory.getLogger(LossDetectionSessionHandler.class);
	long seqNum = Long.MIN_VALUE;

	private SampleConfiguration config;
	public LossDetectionSessionHandler(SampleConfiguration config) {
		this.config = config;
	}
	
	@Override
	public void handle(StompFrame message) {

		logger.info("Content={}", new String(message.getContent()));
		JSONParser parser = new JSONParser();
		try {
			JSONObject test= (JSONObject) parser.parse(new String(message.getContent()));
			long seqId = (long)test.get("sequence");
			logger.info("Seq id - " + seqId + " and seqNum - " + seqNum);
			JSONArray sessions = (JSONArray)test.get("sessions");
			if (seqNum == seqId) {
				logger.info("Duplicate msg. return");
				return;
			}
			if (seqNum < 0) {
				logger.info("First msg. Init seqNum");
				seqNum = seqId-1;
			}
			SessionDirectoryJsonObjectsProcessor processor = new SessionDirectoryJsonObjectsProcessor();
			if(seqId == 0) {
				logger.info("SeqId is 0.Clearcahce");
				processor.clearcache();
				seqNum = -1;
			}
			
			if (seqId == seqNum+1) {
				seqNum = seqId;
				logger.info("update dir and print all sessions");
				processor.processSessionObjects(sessions);
			} else {
				logger.info("seq num mismatch. update seqNum and make a REST query");
				OffsetDateTime timestamp = (seqId<seqNum) ? null:SessionDirectoryData.getTimestamp();
				seqNum = seqId;		
				SessionDirectoryBulkDownloadHelper blkDwnldHlpr = SessionDirectoryBulkDownloadHelper.getInstance();
				blkDwnldHlpr.setTimestamp(timestamp);
				blkDwnldHlpr.bulkDownloadData();
			}
		} catch (org.json.simple.parser.ParseException e) {
			logger.error("Parse Exception - ", e);
		}
	}
}
