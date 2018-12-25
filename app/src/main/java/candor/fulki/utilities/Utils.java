package candor.fulki.utilities;

public class Utils {


    /*


    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .build();
                    ImageLoader.getInstance().displayImage(image_url, imageView, options);

*/

    // if data is available or not

/*

    private boolean isDataAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


*/


/*

    private Task<Void> addRating(String mUserID  , int factor) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "addRating:   function calledd !!!!");
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        return firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            long curRating = ratings.getRating();
            long nextRating = curRating + factor;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }


*/




}
