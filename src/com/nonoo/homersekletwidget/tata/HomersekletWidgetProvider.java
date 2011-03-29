package com.nonoo.homersekletwidget.tata;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.nonoo.homersekletwidget.tata.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

public class HomersekletWidgetProvider extends AppWidgetProvider {
    public static final String URI_SCHEME = "HomersekletWidgetScheme"; 

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		remoteViews.setTextColor(R.id.location, Color.WHITE);
		remoteViews.setTextViewText(R.id.location, "Frissítés...");
		for (int appWidgetId : appWidgetIds)
			AppWidgetManager.getInstance(context).updateAppWidget( appWidgetId, remoteViews );

		Bundle result = downloadData();
    	if( result.getString("errorMessage") != null ) {
    		remoteViews.setTextViewText(R.id.location, result.getString("errorMessage"));
    		remoteViews.setTextColor(R.id.location, Color.RED);
    	} else {
			remoteViews.setTextViewText(R.id.temperature, result.getString("temperature"));
			remoteViews.setTextViewText(R.id.temperature2, result.getString("temperature"));
			remoteViews.setTextViewText(R.id.date, result.getString("date"));
			remoteViews.setTextViewText(R.id.location, result.getString("location"));
    	}

		for (int appWidgetId : appWidgetIds) {
			PendingIntent pendingIntent = makePendingIntent(context, appWidgetId);
			remoteViews.setOnClickPendingIntent(R.id.mainLayout, pendingIntent);
			AppWidgetManager.getInstance(context).updateAppWidget( appWidgetId, remoteViews );
		}
	}

    public PendingIntent makePendingIntent( Context context, int appWidgetId ) {
    	Intent widgetUpdate = new Intent();
    	widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    	widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });

        // make this pending intent unique by adding a scheme to it
        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
        return PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public Bundle downloadData() {
    	Bundle result = new Bundle();
    	XMLHandler responseHandler = new XMLHandler();

    	try {
        	HttpClient httpClient = new DefaultHttpClient();
        	HttpGet httpGet = new HttpGet("http://w2.nonoo.hu/homerseklet/kint.xml");
     
            HttpResponse response = httpClient.execute(httpGet);
            if(response.getStatusLine().getStatusCode() == 200) {
                InputStream is = response.getEntity().getContent();
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();

                xr.setContentHandler(responseHandler);
                xr.parse(new InputSource(is));
            	is.close();

            	result.putString("temperature", responseHandler.temperature);
            	result.putString("location", responseHandler.dataLocation);
            	if( responseHandler.timestamp == 0 )
            		result.putString("date", "n/a");
            	else {
	    			Calendar cal = Calendar.getInstance();
	    			cal.setTimeInMillis(responseHandler.timestamp*1000);
	    			Date date = cal.getTime();
	    			String month = (date.getMonth() < 9 ? "0" + String.valueOf(date.getMonth() + 1) : String.valueOf(date.getMonth() + 1));
	    			String day = (date.getDate() < 10 ? "0" + String.valueOf(date.getDate()) : String.valueOf(date.getDate()));
	    			String hour = (date.getHours() < 10 ? "0" + String.valueOf(date.getHours()) : String.valueOf(date.getHours()));
	    			String minute = (date.getMinutes() < 10 ? "0" + String.valueOf(date.getMinutes()) : String.valueOf(date.getMinutes()));
	    			String sec = (date.getSeconds() < 10 ? "0" + String.valueOf(date.getSeconds()) : String.valueOf(date.getSeconds()));
	    			result.putString("date", String.valueOf(date.getYear()+1900) + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + sec);
            	}
            }
            else
            {
            	result.putString( "errorMessage", String.format("Hiba %1$d: %2$s", +
    				response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()) );
            }

    		httpClient.getConnectionManager().shutdown();
		} catch (SAXException e) {
        	result.putString("errorMessage", "Hiba: XML hiba");
        } catch (Exception e) {
	    	result.putString("errorMessage", "Hiba");
        }

    	return result;
    }
}
