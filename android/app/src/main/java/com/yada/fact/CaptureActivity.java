package com.yada.fact;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.json.JsonFactory;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.yada.fact.APIModel.SearchAPIModel.Item;
import com.yada.fact.APIModel.SearchAPIModel.SearchResult;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CaptureActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyAURMzf4MhvIKwa5ljgRvTuo34rQ5WNwN8";
    private static final String TAG = CaptureActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Feature feature;
    private String[] visionAPI = new String[]{"LANDMARK_DETECTION", "LOGO_DETECTION", "SAFE_SEARCH_DETECTION", "IMAGE_PROPERTIES", "LABEL_DETECTION"};
    private String api = visionAPI[4];

    private EditText mSelectedLabel;
    private Spinner mMealTypeSpinner;
    private EditText mCalories;
    private Spinner mSpinner;
    private Button mAddBtn;

    private UsdaAPI usdaAPI;

    private int mealType = 0;

    @Override
    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        usdaAPI = new UsdaAPI();

        mSelectedLabel = findViewById(R.id.dishNameAC);
        mMealTypeSpinner = findViewById(R.id.meal_type_spinner);
        mCalories = findViewById(R.id.meal_calories);

        mAddBtn = findViewById(R.id.bt_add);
        mSpinner = findViewById(R.id.vision_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String label = (String) adapterView.getItemAtPosition(i);
                mSelectedLabel.setText(label);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> mealTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "Breakfast", "Lunch", "Dinner", "Snack" });
        mealTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMealTypeSpinner.setAdapter(mealTypeAdapter);

        mMealTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Meal type selected: " + (i + 1));
                mealType = i + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "Nothing selected for meal type");
                mealType = 0;
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String selectedLabel = mSelectedLabel.getText().toString();
                String caloriesStr = mCalories.getText().toString();
                if (!selectedLabel.isEmpty() && !caloriesStr.isEmpty() && mealType != 0) {
                    Log.d(TAG, "User has entered name, meal type, and calories");
                    float calories = Float.parseFloat(caloriesStr);

                    Log.d(TAG, "Logging food item: " + selectedLabel + " meal type: " + mealType + " calories: " + calories);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    long startTime = cal.getTimeInMillis();
                    cal.add(Calendar.MILLISECOND, 1);
                    long endTime = cal.getTimeInMillis();

                    DataSource dataSource =
                            new DataSource.Builder()
                            .setAppPackageName(CaptureActivity.this)
                            .setDataType(DataType.TYPE_NUTRITION)
                            .setType(DataSource.TYPE_RAW)
                            .build();

                    DataPoint food = DataPoint.create(dataSource);
                    food.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                    food.getValue(Field.FIELD_FOOD_ITEM).setString(selectedLabel);
                    food.getValue(Field.FIELD_MEAL_TYPE).setInt(mealType);
                    food.getValue(Field.FIELD_NUTRIENTS).setKeyValue(Field.NUTRIENT_CALORIES, calories);

                    DataSet dataSet = DataSet.create(dataSource);
                    dataSet.add(food);

                    Fitness.getHistoryClient(CaptureActivity.this, GoogleSignIn.getLastSignedInAccount(CaptureActivity.this))
                            .insertData(dataSet).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Successfully added food item to food log: " + selectedLabel);
                            Toast.makeText(CaptureActivity.this, "Added " + selectedLabel + " to food log", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                else if (!selectedLabel.isEmpty()) {
                    Log.d(TAG, "Selected label is not empty: " + selectedLabel);
                    ArrayList<Item> items = null;
                    try {
                        items = new AsyncTask<Void, Void, ArrayList<Item>>() {
                            @Override
                            protected ArrayList<Item> doInBackground(Void... params) {
                                return usdaAPI.searchQueryFromUSDA("fast food " + selectedLabel).getResultList().getListOfItems();
                            }
                        }.execute().get();
                    } catch (Exception e) {

                    }

                    if (items != null) {
                        ArrayList<Item> filteredItems = new ArrayList<>(items);
                        CollectionUtils.filter(filteredItems, new Predicate<Item>() {
                            @Override
                            public boolean evaluate(Item object) {
                                return object.getName().toLowerCase().startsWith(selectedLabel.toLowerCase())
                                        || object.getName().toLowerCase().startsWith("fast food") && object.getName().toLowerCase().contains(selectedLabel.toLowerCase());
                            }
                        });

                        if (filteredItems.size() == 0) {
                            filteredItems = items;
                        }

                        List<String> queryArray = new ArrayList<>(Arrays.asList(selectedLabel.toLowerCase().split(" ")));
                        int bestMatchSize = Integer.MAX_VALUE;

                        Item bestMatch = null;

                        for (Item item : filteredItems) {
                            Log.d(TAG, "Name: " + item.getName());
                            Log.d(TAG, "NDB No.: " + item.getNdbno());
                            List<String> itemArray = new ArrayList<>(Arrays.asList(item.getName().toLowerCase().split(" ")));
                            Collection<String> disjunction = CollectionUtils.disjunction(queryArray, itemArray);
                            if (disjunction.size() < bestMatchSize) {
                                Log.d(TAG, "Found better match");
                                bestMatchSize = disjunction.size();
                                bestMatch = item;
                            }
                        }

                        if (bestMatch != null) {
                            Log.d(TAG, "Best match is : " + bestMatch.getName());
                        }
                    }
                }
                else {
                    Log.d(TAG, "Selected label is empty");
                }
            }
        });

        feature = new Feature();
        feature.setType(visionAPI[4]);
        feature.setMaxResults(5);

    }

    public void callCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView user_image = findViewById(R.id.iv_image);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            user_image.setImageBitmap(imageBitmap);
            callCloudVision(imageBitmap, feature);
        }
    }
    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        // Base64 encode the JPEG
        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }

    @SuppressLint("StaticFieldLeak")
    private void callCloudVision(final Bitmap bitmap, final Feature feature) {
        final List<Feature> featureList = new ArrayList<>();
        featureList.add(feature);

        final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

        AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(featureList);
        annotateImageReq.setImage(getImageEncodeImage(bitmap));
        annotateImageRequests.add(annotateImageReq);


        List<EntityAnnotation> entityAnnotations = null;
        try {
            entityAnnotations = new AsyncTask<Void, Void, List<EntityAnnotation>>() {
                @Override
                protected List<EntityAnnotation> doInBackground(Void... params) {
                    try {

                        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                        VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                        builder.setVisionRequestInitializer(requestInitializer);

                        Vision vision = builder.build();

                        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                        batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                        Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                        annotateRequest.setDisableGZipContent(true);
                        BatchAnnotateImagesResponse response = annotateRequest.execute();

                        return response.getResponses().get(0).getLabelAnnotations();
                    } catch (GoogleJsonResponseException e) {
                        Log.d(TAG, "failed to make API request because " + e.getContent());
                    } catch (IOException e) {
                        Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
                    }

                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {

        }

        if (entityAnnotations != null) {
            List<String> labels = new ArrayList<>(CollectionUtils.collect(entityAnnotations, new Transformer<EntityAnnotation, String>() {
                @Override
                public String transform(EntityAnnotation input) {
                    return input.getDescription();
                }
            }));

            for (String label : labels) {
                Log.d(TAG, "Label: " + label);
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(arrayAdapter);



            for (EntityAnnotation annotation : entityAnnotations) {
                Log.d(TAG, "Label: " + annotation.getDescription() + " score: " + annotation.getScore());
            }
        }
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotations;

        String message = "";
        switch (api) {
            case "LANDMARK_DETECTION":
                entityAnnotations = imageResponses.getLandmarkAnnotations();
                message = formatAnnotation(entityAnnotations);
                break;
            case "LOGO_DETECTION":
                entityAnnotations = imageResponses.getLogoAnnotations();
                message = formatAnnotation(entityAnnotations);
                break;
            case "SAFE_SEARCH_DETECTION":
                SafeSearchAnnotation annotation = imageResponses.getSafeSearchAnnotation();
                message = getImageAnnotation(annotation);
                break;
            case "LABEL_DETECTION":
                entityAnnotations = imageResponses.getLabelAnnotations();
                message = formatAnnotation(entityAnnotations);
                break;
        }
        return message;
    }

    private String formatAnnotation(List<EntityAnnotation> entityAnnotation) {
        String message = "";

        if (entityAnnotation != null) {
            for (EntityAnnotation entity : entityAnnotation) {
                message = message + "    " + entity.getDescription() + " " + entity.getScore();
                message += "\n";
            }
        } else {
            message = "Nothing Found";
        }
        return message;
    }

    private String getImageAnnotation(SafeSearchAnnotation annotation) {
        return String.format("adult: %s\nmedical: %s\nspoofed: %s\nviolence: %s\n",
                annotation.getAdult(),
                annotation.getMedical(),
                annotation.getSpoof(),
                annotation.getViolence());
    }

}

