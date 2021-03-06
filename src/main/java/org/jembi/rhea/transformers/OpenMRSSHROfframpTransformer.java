/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jembi.rhea.transformers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jembi.rhea.RestfulHttpRequest;
import org.jembi.rhea.Constants;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class OpenMRSSHROfframpTransformer extends AbstractMessageTransformer {

	@Override
	public Object transformMessage(MuleMessage msg, String enc)
			throws TransformerException {
		
		RestfulHttpRequest request = (RestfulHttpRequest) msg.getPayload();
		String path = request.getPath();
		StringTokenizer st = new StringTokenizer(path, "/");
		String id_str = null;
		for (int i = 0 ; i < 5 ; i++) {
			id_str = st.nextToken();
		}
		
		String idType = null;
		String patientId = null;
		int index = id_str.indexOf("-");
		if (index < 0) {
			return null;
		} else {
			idType = id_str.substring(0, index);
			patientId = id_str.substring(index + 1);
		}
		
		Map<String, String> origRequestParams = request.getRequestParams();
		String startDate = origRequestParams.get(Constants.QUERY_ENC_START_DATE_PARAM);
		String endDate = origRequestParams.get(Constants.QUERY_ENC_END_DATE_PARAM);
		
		String notificationType = origRequestParams.get(Constants.QUERY_ENC_NOTIFICATION_TYPE_PARAM);
		String ELID = origRequestParams.get(Constants.QUERY_ENC_ELID_PARAM);
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
		
		request.setPath("openmrs/ws/rest/RHEA/patient/encounters");
		
		Map<String, String> newRequestParams = new HashMap<String, String>();
		
		try {
			if (startDate != null && !startDate.isEmpty()) {
				Date date = sdf1.parse(startDate);
				
				// Add 1 day to the date as the SHR doesn't support timestamps
				// thus we need to make this the next day to prevent dupes
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				c.add(Calendar.DATE, 1);
				
				startDate = sdf2.format(c.getTime());
				
				newRequestParams.put("dateStart", startDate);
			}
			
			if (endDate != null && !startDate.isEmpty()) {
				Date date = sdf1.parse(endDate);
				endDate = sdf2.format(date);
				
				newRequestParams.put("dateEnd", endDate);
			}
		} catch (ParseException e) {
			throw new TransformerException(this, e);
		}
		
		newRequestParams.put("patientId", patientId);
		newRequestParams.put("idType", idType);
		if (notificationType != null && !notificationType.isEmpty()) {
			newRequestParams.put("notificationType", notificationType);
		}
		if (ELID != null && !ELID.isEmpty()) {
			newRequestParams.put("elid", ELID);
		}
		
		request.setRequestParams(newRequestParams);
		
		return msg;
	}

}
