package com.android.imagesearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 4;
    private LruCache<String,Bitmap> imageCache = new LruCache<String, Bitmap>(cacheSize){
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };
    private String searchString;
    private int offset = 0;
    private JSONArray images = new JSONArray() ;
    private GridView gridview;
    private int STATE = 0;
    private LinearLayout searchLayout;
    private FloatingActionButton fab;
    private boolean CONTINUE = false;
    private ImageAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
        searchLayout.setTranslationY(1000.0F);



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                STATE =1 ;
                searchLayout.animate().translationY(0.0F);
                fab.setVisibility(View.GONE);
                findViewById(R.id.suggestion).setVisibility(View.GONE);

            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.animate().translationY(1000.0F);
                fab.setVisibility(View.VISIBLE);
                STATE = 0;
            }
        });


        final EditText searchBox = (EditText) findViewById(R.id.search_string);
        gridview = (GridView) findViewById(R.id.gridview);
        adapter = new ImageAdapter(MainActivity.this,imageCache);
        gridview.setAdapter(adapter);
        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchBox.getText().length()>0)
                {
                    searchString = searchBox.getText().toString();
                    offset =0 ;
                    images = null;
                    images = new JSONArray();
                    loadImages();
                    searchLayout.animate().translationY(1000.0F);
                    fab.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    STATE = 0;
                }
                else
                {
                    Snackbar.make(v,"Search String cant be empty",Snackbar.LENGTH_LONG).show();
                }

            }
        });









    }

    @Override
    public void onBackPressed() {

        if(STATE==1)
        {
            searchLayout.animate().translationY(1000.0F);
            fab.setVisibility(View.VISIBLE);
            STATE = 0;
        }
        else
        super.onBackPressed();
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Context mContext;
        private LruCache<String, Bitmap> imageCache;
        private JSONObject data;

        public ImageAdapter(Context c, LruCache<String,Bitmap> imageCache) {
            mContext = c;
            this.imageCache = imageCache;
            this.data = data;
            inflater = (LayoutInflater)mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return images.length();
        }


        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            View rowView;
            if (convertView == null) {
                 rowView = inflater.inflate(R.layout.grid_item, null);
            } else {
                rowView = convertView ;
            }

            imageView = (ImageView) rowView.findViewById(R.id.imageview);

            try {



                new ImageDownloaderTask(imageView, imageCache).execute(images.getString(position));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return imageView;
        }
    }


    public void loadImages()
    {
        String URL = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=50&pilimit=50&generator=prefixsearch&gpssearch="+searchString+"&gpsoffset="+offset;



        NetworkAsyncTask getData = new NetworkAsyncTask(MainActivity.this, null, "", URL, new AsyncCommunicator() {
            @Override
            public void AsyncEvent(JSONObject value, String taskid) {

                try {

                    if(value==null) return ;

                    if(value.has("success"))
                        Snackbar.make(searchLayout,"No Internet Connection",Snackbar.LENGTH_LONG).show();



                    JSONObject a = value.getJSONObject("query");
                    JSONObject b = a.getJSONObject("pages");

                    Iterator<String> keys =  b.keys();
                    while (keys.hasNext())
                    {
                        String key = keys.next();
                        JSONObject c = b.getJSONObject(key);

                        if(c.has("thumbnail"))
                        {
                            images.put(c.getJSONObject("thumbnail").getString("source"));
                        }
                        else
                        {
                            images.put("https://cdn.amctheatres.com/Media/Default/Images/noposter.jpg");
                        }


                    }


                    if(value.has("continue"))
                    {

                        JSONObject c = value.getJSONObject("continue");
                        offset = c.getInt("gpsoffset");
                        loadImages();
                    }

                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        getData.execute();


    }

}
