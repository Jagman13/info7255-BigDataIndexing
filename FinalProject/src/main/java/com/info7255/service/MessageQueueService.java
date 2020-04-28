package com.info7255.service;

import com.info7255.dao.MessageQueueDao;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * Created by jagman on Apr, 2020
 **/
@Service
public class MessageQueueService {
	
	@Autowired
	private MessageQueueDao messageQueueDao;

	public void addToMessageQueue(String message, boolean isDelete) {
		JSONObject object = new JSONObject();
		object.put("message", message);
		object.put("isDelete", isDelete);

		// save plan to message queue "messageQueue"
		messageQueueDao.addToQueue("messageQueue", object.toString());
		System.out.println("Message saved successfully: " + object.toString());
	}
}
