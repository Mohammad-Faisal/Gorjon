package candor.fulki.activities;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import candor.fulki.adapters.DIistrictsAdapter;
import candor.fulki.adapters.ListPeopleAdapter;
import candor.fulki.models.UserBasic;
import candor.fulki.R;

public class SearchActivityF extends AppCompatActivity {

    private static final String TAG = "SearchActivityF";

    FirebaseFirestore firebaseFirestore;
    public Query query;
    EditText mSearchBoxText;



    private RecyclerView mPeopleList;
    private LinearLayoutManager mLinearLayout;
    private ListPeopleAdapter mPeopleAdapter;

    String districtString = "All Bangladesh";
    String categoryString = "All Category";
    String bloodString = "Blood";
    TextView mDistrict;
    TextView mCategory;
    TextView mBlood;


    //district filtering
    private DIistrictsAdapter districtDIistrictsAdapter;
    AlertDialog districtAlertDialog;

    private ArrayList<String> mStringList;

    private static final String[] districts={
            "Barguna" , "Barisal" , "Bhola",    "Jhalokati",  "Patuakhali", "Pirojpur",
            "Bandarban","Brahmanbaria",   "Chandpur", "Chittagong", "Comilla",    "Cox's Bazar","Feni",     "Khagrachhari","Lakshmipur", "Noakhali", "Rangamati",
            "Dhaka",    "Faridpur" , "Gazipur",  "Gopalganj",  "Kishoreganj","Madaripur",  "Manikganj","Munshiganj",  "Narayanganj","Narsingdi","Rajbari","Shariatpur","Tangail",
            "Bagerhat", "Chuadanga",      "Jessore",  "Jhenaidah",  "Khulna",     "Kushtia",    "Magura",   "Meherpur",    "Narail",     "Satkhira",
            "Jamalpur", "Mymensingh",     "Netrakona","Sherpur",
            "Bogra",    "Chapainawabganj","Joypurhat","Naogaon",    "Natore",     "Pabna",      "Rajshahi", "Sirajganj",
            "Dinajpur", "Gaibandha",      "Kurigram", "Lalmonirhat","Nilphamari", "Panchagarh", "Rangpur",  "Thakurgaon",
            "Habiganj", "Moulvibazar",    "Sunamganj","Sylhet"
    };

    private static final String[] categories = {
        "Child Marrige" , "Violence" , "Environment" , "Women Empowerment","Education"
    };

    private static final String[] bloods = {
            "A+" , "A-" , "B+" , "B-","AB+" ,"AB-" , "O+" , "O-"
    };

    private ArrayList<String> mStringListCategory;
    private ArrayList<String> mStringListBlood = new ArrayList<>();


    HashMap< String  , String> ChildMarrige = new HashMap<>();
    HashMap< String  , String> Violence= new HashMap<>();
    HashMap< String  , String> Environment= new HashMap<>();
    HashMap< String  , String> WomenEmpowerment= new HashMap<>();
    HashMap< String  , String> Education= new HashMap<>();


    boolean bloodFlag  = false , categoryFlag = false , districtFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        firebaseFirestore = FirebaseFirestore.getInstance();


        getSupportActionBar().setTitle("Search people");
        getSupportActionBar().setHomeButtonEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);

        mDistrict = findViewById(R.id.search_activity_district);
        mCategory  = findViewById(R.id.search_activity_category);
        mBlood  =findViewById(R.id.search_activity_blood);

        initDistrictData();
        initCategoryData();
        initBloodData();


        mDistrict.setText("District");
        mCategory.setText("Category");
        mBlood.setText("Blood");

        loadAllData();


        mSearchBoxText = findViewById(R.id.search_text_input);
        //mSearchBoxText.addTextChangedListener(filterTextWatcher);



        Client client = new Client( "YWTL46QL1P" , "fcdc55274ed56d6fb92f51c0d0fc46a0" );
        Index index = client.getIndex("users");


        mSearchBoxText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Index index = client.getIndex("users");
                Query query = new Query(s.toString())
                        .setAttributesToRetrieve("name", "user_name"  , "thumb_image", "district" , "contact_no" , "blood_group" , "user_id")
                        .setHitsPerPage(10);
                index.searchAsync(query, (content, error) -> {
                    try {
                        JSONArray hits = content.getJSONArray("hits");
                        List < UserBasic > userBasicList = new ArrayList<>();
                        Log.d(TAG, "afterTextChanged:     "+hits.length());
                        for(int i=0;i<hits.length();i++){
                            JSONObject jsonObject = hits.getJSONObject(i);
                            Log.d(TAG, "afterTextChanged:  "+jsonObject.toString());
                            UserBasic userBasic = new UserBasic();

                            String district = jsonObject.getString("district");
                            String blood = jsonObject.getString("blood_group");
                            String uid = jsonObject.getString("user_id");
                            String name = jsonObject.getString("user_name");

                            Log.d(TAG, "afterTextChanged: from net    found district   "+district);
                            Log.d(TAG, "afterTextChanged: from net    found blood   "+blood);
                            Log.d(TAG, "afterTextChanged: from net    found name   "+name);

                            Log.d(TAG, "afterTextChanged: from textbox    found district   "+districtString);
                            Log.d(TAG, "afterTextChanged: from textbox    found blood   "+bloodString);
                            Log.d(TAG, "afterTextChanged: from textbox    found category   "+categoryString);

                            userBasic.setmUserName(jsonObject.getString("name"));
                            userBasic.setmUserID(jsonObject.getString("user_id"));
                            userBasic.setmUserThumbImage(jsonObject.getString("thumb_image"));

                            if(districtFlag && bloodFlag){
                                if(districtString.equals(district) && bloodString.equals(blood)){
                                    if(categoryFlag){
                                        Log.d(TAG, "afterTextChanged: bloodFlag districtFlag category also enabled"+categoryString);
                                        boolean flag = processCategory(uid ,categoryString);
                                        if(flag){
                                            userBasicList.add(userBasic);
                                        }
                                    }else{
                                        Log.d(TAG, "afterTextChanged: districtFlag  bloodFlag enabled"+categoryString);
                                        userBasicList.add(userBasic);
                                    }
                                }
                            }else if(districtFlag && !bloodFlag){
                                if(districtString.equals(district)){
                                    Log.d(TAG, "afterTextChanged:   district is enabled");
                                    if(categoryFlag){
                                        Log.d(TAG, "afterTextChanged: districtFlag  category enabled"+categoryString);
                                        boolean flag = processCategory(uid ,categoryString);
                                        if(flag){
                                            userBasicList.add(userBasic);
                                        }
                                    }else{
                                        Log.d(TAG, "afterTextChanged: only  districtFlag enabled"+categoryString);

                                        userBasicList.add(userBasic);
                                    }
                                }
                            }else if(!districtFlag && bloodFlag){
                                if( bloodString.equals(blood)){
                                    if(categoryFlag){
                                        Log.d(TAG, "afterTextChanged: bloodFlag  category also enabled"+categoryString);
                                        boolean flag = processCategory(uid ,categoryString);
                                        if(flag){
                                            userBasicList.add(userBasic);
                                        }
                                    }else{
                                        Log.d(TAG, "afterTextChanged: only  bloodFlag enabled"+categoryString);
                                        userBasicList.add(userBasic);
                                    }
                                }

                            }else{
                                if(categoryFlag){
                                    Log.d(TAG, "afterTextChanged: only  category enabled"+categoryString);
                                    boolean flag = processCategory(uid ,categoryString);
                                    if(flag){
                                        userBasicList.add(userBasic);
                                    }
                                }else{
                                    Log.d(TAG, "afterTextChanged: nothing enabled"+categoryString);
                                    userBasicList.add(userBasic);
                                }
                            }
                        }

                        Log.d(TAG, "afterTextChanged:    "+userBasicList.size());

                        mPeopleAdapter = new ListPeopleAdapter(userBasicList , SearchActivityF.this , SearchActivityF.this);
                        mPeopleList = findViewById(R.id.search_activity_recycler);
                        mLinearLayout = new LinearLayoutManager(SearchActivityF.this);
                        mPeopleList.hasFixedSize();
                        mPeopleList.setLayoutManager(mLinearLayout);
                        mPeopleList.setAdapter(mPeopleAdapter);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private boolean processCategory(String uid , String categoryString){
        if(categoryString.equals("Child Marrige")){
            return ChildMarrige.get(uid) != null && ChildMarrige.get(uid).equals("y");
        }else if(categoryString.equals("Violence")){
            return Violence.get(uid) != null && Violence.get(uid).equals("y");

        }else if(categoryString.equals("Environment")){
            return Environment.get(uid) != null && Environment.get(uid).equals("y");

        }else if(categoryString.equals("Women Empowerment")){
            return WomenEmpowerment.get(uid) != null && WomenEmpowerment.get(uid).equals("y");
        }else if(categoryString.equals("Education")){
            return Education.get(uid) != null && Education.get(uid).equals("y");
        }else{
            return  true;
        }
    }

    private void initBloodData() {
        mStringListBlood=new ArrayList<String>();

        for(int i=0;i<bloods.length;i++){
            mStringListBlood.add(bloods[i]);
        }


        mBlood.setOnClickListener(v -> {

            districtDIistrictsAdapter =new DIistrictsAdapter(mStringListBlood,SearchActivityF.this);
            districtAlertDialog = new AlertDialog.Builder(SearchActivityF.this).create();
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.custom_district_list, null);
            final EditText editText=convertView.findViewById(R.id.distric_list_search_text);
            editText.setVisibility(View.GONE);
            final ListView lv =  convertView.findViewById(R.id.distric_list_listview);
            districtAlertDialog.setView(convertView);
            districtAlertDialog.setCancelable(false);

            lv.setAdapter(districtDIistrictsAdapter);
            districtAlertDialog.show();
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    districtDIistrictsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            lv.setOnItemClickListener((parent, view, position, id) -> {
                bloodFlag = true;
                mBlood.setText(lv.getItemAtPosition(position).toString());
                bloodString = lv.getItemAtPosition(position).toString();
                Log.d(TAG, "Blood Data:    found a Blood  "+lv.getItemAtPosition(position).toString());
                districtAlertDialog.dismiss();
            });
        });
    }

    private void initDistrictData() {

        mStringList=new ArrayList<String>();

        for(int i=0;i<districts.length;i++){
            mStringList.add(districts[i]);
        }


        mDistrict.setOnClickListener(v -> {

            districtDIistrictsAdapter =new DIistrictsAdapter(mStringList,SearchActivityF.this);
            districtAlertDialog = new AlertDialog.Builder(SearchActivityF.this).create();
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.custom_district_list, null);
            final EditText editText=convertView.findViewById(R.id.distric_list_search_text);
            final ListView lv =  convertView.findViewById(R.id.distric_list_listview);
            districtAlertDialog.setView(convertView);
            districtAlertDialog.setCancelable(false);

            lv.setAdapter(districtDIistrictsAdapter);
            districtAlertDialog.show();
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    districtDIistrictsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            lv.setOnItemClickListener((parent, view, position, id) -> {
                districtFlag = true;
                mDistrict.setText(lv.getItemAtPosition(position).toString());
                districtString = lv.getItemAtPosition(position).toString();
                Log.d(TAG, "initDistrictData:    found a district  "+lv.getItemAtPosition(position).toString());
                districtAlertDialog.dismiss();
            });
        });
    }

    private void initCategoryData() {

        mStringListCategory=new ArrayList<String>();

        for(int i=0;i<categories.length;i++){
            mStringListCategory.add(categories[i]);
        }


        mCategory.setOnClickListener(v -> {

            districtDIistrictsAdapter =new DIistrictsAdapter(mStringListCategory,SearchActivityF.this);
            districtAlertDialog = new AlertDialog.Builder(SearchActivityF.this).create();
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.custom_district_list, null);
            final EditText editText=convertView.findViewById(R.id.distric_list_search_text);
            editText.setVisibility(View.GONE);
            final ListView lv =  convertView.findViewById(R.id.distric_list_listview);
            districtAlertDialog.setView(convertView);
            districtAlertDialog.setCancelable(false);

            lv.setAdapter(districtDIistrictsAdapter);
            districtAlertDialog.show();
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    districtDIistrictsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            lv.setOnItemClickListener((parent, view, position, id) -> {
                categoryFlag = true;
                mCategory.setText(lv.getItemAtPosition(position).toString());
                categoryString = lv.getItemAtPosition(position).toString();
                Log.d(TAG, "initDistrictData:    found a category  "+lv.getItemAtPosition(position).toString());
                districtAlertDialog.dismiss();
            });
        });
    }


    private void loadAllData(){
        firebaseFirestore.collection("child_marrige").get().addOnSuccessListener(documentSnapshots -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            String uid = doc.getDocument().getString("user_id");
                            Log.d(TAG, "onSuccess:   found user id in child_marrige "+uid);
                            ChildMarrige.put(uid , "y");
                        }
                    }
                }
            }
        });
        firebaseFirestore.collection("education").get().addOnSuccessListener(documentSnapshots -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            String uid = doc.getDocument().getString("user_id");
                            Log.d(TAG, "onSuccess:   found user id in education "+uid);
                            Education.put(uid , "y");
                        }
                    }
                }
            }
        });
        firebaseFirestore.collection("women_empowerment").get().addOnSuccessListener(documentSnapshots -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            String uid = doc.getDocument().getString("user_id");
                            Log.d(TAG, "onSuccess:   found user id in women_empowerment "+uid);
                            WomenEmpowerment.put(uid , "y");
                        }
                    }
                }
            }
        });
        firebaseFirestore.collection("violence").get().addOnSuccessListener(documentSnapshots -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            String uid = doc.getDocument().getString("user_id");
                            Log.d(TAG, "onSuccess:   found user id in violence "+uid);
                            Violence.put(uid , "y");
                        }
                    }
                }
            }
        });
        firebaseFirestore.collection("environment").get().addOnSuccessListener(documentSnapshots -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            String uid = doc.getDocument().getString("user_id");
                            Log.d(TAG, "onSuccess:   found user id in environment "+uid);
                            Environment.put(uid , "y");
                        }
                    }
                }
            }
        });
    }







}
