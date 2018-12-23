package candor.fulki.search;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import candor.fulki.general.ValueAdapter;
import candor.fulki.R;
import candor.fulki.models.UserSearch;


public class SearchActivity extends AppCompatActivity
        implements HorizontalRecyclerAdapterforsearch.OnButtonClickListener1
{
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 2;

    private Context mContext = SearchActivity.this;
    ArrayList<String> subList1 = new ArrayList<>();
    HashMap<String, UserSearch> hashMap = new HashMap<String, UserSearch>();


    //widgets
    private EditText mSearchParam;
    private ImageButton searchbtn;
    private RecyclerView mResultList;
    Query firebaseSearchQuery;





    //search
    private static final String[] districts={
            "Barguna",  "Barisal",        "Bhola",    "Jhalokati",  "Patuakhali", "Pirojpur",
            "Bandarban","Brahmanbaria",   "Chandpur", "Chittagong", "Comilla",    "Cox's Bazar","Feni",     "Khagrachhari","Lakshmipur", "Noakhali", "Rangamati",
            "Dhaka",    "Faridpur",       "Gazipur",  "Gopalganj",  "Kishoreganj","Madaripur",  "Manikganj","Munshiganj",  "Narayanganj","Narsingdi","Rajbari","Shariatpur","Tangail",
            "Bagerhat", "Chuadanga",      "Jessore",  "Jhenaidah",  "Khulna",     "Kushtia",    "Magura",   "Meherpur",    "Narail",     "Satkhira",
            "Jamalpur", "Mymensingh",     "Netrakona","Sherpur",
            "Bogra",    "Chapainawabganj","Joypurhat","Naogaon",    "Natore",     "Pabna",      "Rajshahi", "Sirajganj",
            "Dinajpur", "Gaibandha",      "Kurigram", "Lalmonirhat","Nilphamari", "Panchagarh", "Rangpur",  "Thakurgaon",
            "Habiganj", "Moulvibazar",    "Sunamganj","Sylhet"
    };


    //for district

    private EditText mSearchEdt;

    private ArrayList<UserSearch> users,topicusers;
    private ArrayList<String> uid;
    private ArrayList<String> mStringList;

    private ValueAdapter valueAdapter;
    private String presentdistrict;
    private FirebaseFirestore firebaseFirestore;
    private String userID;
    VerticalRecyclerAdapterforsearch adapterforsearch;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore=FirebaseFirestore.getInstance();
        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        setContentView(R.layout.activity_search1);
        getallusers();
        users=new ArrayList<>();
        topicusers=new ArrayList<>();
        uid=new ArrayList<>();
        mSearchParam = (EditText) findViewById(R.id.search);
        searchbtn=findViewById(R.id.search_btn);
        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));





        Log.d(TAG, "onCreate: started.");


        mSearchEdt=(EditText)findViewById(R.id.txt_search);



        if(userID!=null){
            setSpinner();
            initData();
        }

        subList1.add("child marriage");
        subList1.add("violence");
        subList1.add("education");
        subList1.add("women_empowerment");
        subList1.add("environment");
        subList1.add("A+ blood");
        subList1.add("A- blood");
        subList1.add("B+ blood");
        subList1.add("B- blood");
        subList1.add("AB+ blood");
        subList1.add("AB- blood");
        subList1.add("O+ blood");
        subList1.add("O- blood");

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.topiclistmap);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        HorizontalRecyclerAdapterforsearch adapter = new HorizontalRecyclerAdapterforsearch(subList1,this);
        mRecyclerView.setAdapter(adapter);




        initTextListener();
    }

    public void getallusers(){

        firebaseFirestore.collection("user_search").get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                UserSearch userSearch=document.toObject(UserSearch.class);
                                users.add(userSearch);
                                hashMap.put(userSearch.getUser_id(),userSearch);
                                //City city = documentSnapshot.toObject(City.class);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

//        myRef.child("user_search").orderByKey().addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
//                    UserSearch userSearch=dataSnapshot1.getValue(UserSearch.class);
//                    users.add(userSearch);
//                    hashMap.put(userSearch.getUser_id(),userSearch);
//                    //uid.add(userSearch.getUser_id());
//                }
//                Log.d(TAG, "onDataChange: motuser "+users);
//
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    public void setSpinner() {
        uid.clear();

        firebaseFirestore.collection("user_districts").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String jela= document.getString("district");
                        if(jela==null)jela="Dhaka";

                        firebaseFirestore.collection(jela).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String userid=document.getString("uid");
                                        uid.add(userid);
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

//                        myRef.child("districts").child(jela).addValueEventListener(
//                                new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
//                                            uid.add(dataSnapshot1.getKey());
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                }
//                        );
                        mSearchEdt.setText(jela);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });




    }


    AlertDialog alertDialog;

    private void initData() {

        mStringList=new ArrayList<String>();

        for(int i=0;i<districts.length;i++){
            mStringList.add(districts[i]);
        }





        mSearchEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueAdapter=new ValueAdapter(mStringList,mContext);

                alertDialog = new AlertDialog.Builder(mContext).create();
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.custom_list, null);
                final EditText editText=convertView.findViewById(R.id.sear);
                final ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                alertDialog.setView(convertView);
                alertDialog.setCancelable(false);

                lv.setAdapter(valueAdapter);
                alertDialog.show();
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        valueAdapter.getFilter().filter(s);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSearchEdt.setText(lv.getItemAtPosition(position).toString());
                        uid.clear();




                        firebaseFirestore.collection(lv.getItemAtPosition(position).toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String userid=document.getString("uid");
                                        uid.add(userid);
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                        alertDialog.dismiss();

                    }
                });
            }
        });






    }

    private void initTextListener(){
        adapterforsearch=new VerticalRecyclerAdapterforsearch(users,mContext);
        mResultList.setAdapter(adapterforsearch);
        mResultList.setVisibility(View.GONE);

        mSearchParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterforsearch=new VerticalRecyclerAdapterforsearch(users,mContext);

                mResultList.setAdapter(adapterforsearch);
                mResultList.setVisibility(View.GONE);
            }
        });



        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());

                //searchForMatch(text);
                if(!text.isEmpty()){
                    mResultList.setVisibility(View.VISIBLE);
                    firebaseUserSearch(s);
                }

                else{
                    mResultList.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

//                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
//                searchForMatch(text);
            }
        });
    }






    private void firebaseUserSearch(CharSequence searchText) {
        adapterforsearch.getFilter().filter(searchText);
    }







    @Override
    public void onButtonClick1(String text) {
        Log.d(TAG, "onButtonClick: dekha jak");
        String reps=text;
        if(text.contains(" blood")){
            reps=text.replaceAll(" blood","");

        }
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }



        Log.d(TAG, "onButtonClick1: fidget1"+ text);

        firebaseFirestore.collection(reps).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                topicusers.clear();
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if(uid.contains(document.getString("user_id"))){
                            topicusers.add(hashMap.get(document.getString("user_id")));
                        }
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                    adapterforsearch=new VerticalRecyclerAdapterforsearch(topicusers,mContext);
                    if(topicusers.size()>0){

                        mResultList.setVisibility(View.VISIBLE);
                    }
                    mResultList.setAdapter(adapterforsearch);


                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });



    }
}