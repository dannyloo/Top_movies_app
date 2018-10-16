package com.redBrick.MovieAppDanny;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.redBrick.MovieAppDanny.R.id.listView;

public class GridViewActivity extends ActionBarActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();

    private ListView mListView;
    private ProgressBar mProgressBar;

    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private String FEED_URL = "https://www.fandango.com/rss/newmovies.rss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        mListView = (ListView) findViewById(listView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mListView.setAdapter(mGridAdapter);

        //Start reading XML in background while app loads
        new AsyncHttpTask().execute(FEED_URL);
        mProgressBar.setVisibility(View.VISIBLE);

        onStart();
    }


    @Override
    protected void onStart() {
        super.onStart();
        final WebView webView = new WebView(this);

        //onclick listener for the movies
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                //Load Trailer when clicked
                System.out.println("Screen loc " + mGridData.get(position).getTrailer());
                webView.loadUrl(mGridData.get(position).getTrailer());
                System.out.println(mGridData.get(position).getTrailer());
            }
        });
    }


    //Getting data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(httpResponse.getEntity().getContent());
                    parseResult(FEED_URL);
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete, update UI
            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }


    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     */
    private void parseResult(String urlAddress) {
        System.out.println("url: " + urlAddress);
        //Setting up own grid item class that stores: trailer link, movie title, rating, thumbnail image
        GridItem item;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(urlAddress);
            doc.normalize();

            System.out.println("root " + doc.getDocumentElement().getNodeName());

            NodeList movieList = doc.getElementsByTagName("item");
            System.out.println("Movies " + movieList.getLength());


            for(int i = 0; i < movieList.getLength(); i++){
                item = new GridItem();
                Node showNode = movieList.item(i);
                Element showElement = (Element)showNode;

                //Get movie title
                NodeList titleList = showElement.getElementsByTagName("title");
                Element titleElement = (Element)titleList.item(0);
                String showName = titleElement.getFirstChild().getNodeValue();
                item.setTitle(showName);

                //Get fan rating, if non is available replace with "No rating available"
                NodeList ratingList = showElement.getElementsByTagName("fan:fanRating");
                if(ratingList.getLength() > 0){
                    Element ratingElement = (Element)ratingList.item(0);
                    String ratingName = ratingElement.getFirstChild().getNodeValue();
                    //System.out.println(ratingName);
                    item.setFanRating(ratingName + "/5" + "\t\t\t" + "stars");
                }else{
                    //System.out.println("No rating");
                    item.setFanRating("No rating available");
                }

                //Get trailer link
                NodeList trailerList = showElement.getElementsByTagName("fan:trailer");
                Element trailerElement = (Element)trailerList.item(0);
                String trailerName = trailerElement.getFirstChild().getNodeValue();
                item.setTrailer(trailerName);

                //Get picture thumbnail
                NodeList pictureList = showElement.getElementsByTagName("enclosure");
                Element pictureElement = (Element)pictureList.item(0);
                if(pictureElement.hasAttribute("url")){
                    //System.out.println(pictureElement.getAttribute("url"));
                    item.setImage(pictureElement.getAttribute("url"));
                }
                else {
                    Toast.makeText(GridViewActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
                }

                mGridData.add(item);

            }

        //Error handling
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }












        /*GridItem item;
        try {

            URL rssURL = new URL(urlAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
            String title = "";
            String imageUrl = "";
            String trailerUrl = "";
            Double fanRating = 0.0;
            String line;
            while ((line = in.readLine()) != null) {
                int titleEndIndex = 0;
                int titleStartIndex = 0;
                int imageStartIndex = 0;
                int imageEndIndex = 0;
                int trailerStartIndex = 0;
                int trailerEndIndex = 0;
                int fanRatingStartIndex = 0;
                int fanRatingEndIndex = 0;

                while (titleStartIndex >= 0 && imageStartIndex >=0 && trailerStartIndex >= 0) {
                    titleStartIndex = line.indexOf("<title><![CDATA[", titleEndIndex);
                    imageStartIndex = line.indexOf("<enclosure url=\"", imageEndIndex);
                    trailerStartIndex = line.indexOf("<fan:trailer><![CDATA[", trailerEndIndex);
                    fanRatingStartIndex = line.indexOf("<fan:fanRating><![CDATA[", fanRatingEndIndex);

                    if (titleStartIndex >= 0 && imageEndIndex >= 0 && trailerEndIndex >= 0) {
                        item = new GridItem();

                        titleEndIndex = line.indexOf("]]></title>", titleStartIndex);
                        title = line.substring(titleStartIndex + "<title><![CDATA[".length(), titleEndIndex) + "\n";

                        imageEndIndex = line.indexOf("\" length=", imageStartIndex);
                        imageUrl = line.substring(imageStartIndex + "<enclosure url=\"".length(), imageEndIndex) + "\n";

                        trailerEndIndex = line.indexOf("]]></fan:trailer>", trailerStartIndex);
                        trailerUrl = line.substring(trailerStartIndex + "<fan:trailer><![CDATA[".length(), trailerEndIndex) + "\n";

                        fanRatingEndIndex = line.indexOf("]]></fan:fanRating>", fanRatingStartIndex);
                        fanRating = Double.parseDouble(line.substring(fanRatingStartIndex + "<fan:fanRating><![CDATA[".length(), fanRatingEndIndex) + "\n");

                        item.setImage(imageUrl);
                        item.setTitle(title);
                        item.setTrailer(trailerUrl);
                        item.setFanRating(fanRating);

                        System.out.println(item.getFanRating());
                        mGridData.add(item);
                    }
                }


                System.out.println("imageurl " + mGridData.get(1).getTrailer());
            }

            in.close();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }

}