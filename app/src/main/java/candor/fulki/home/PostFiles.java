package candor.fulki.home;

public class PostFiles {
    String imagePath;
    String imageThumbPath;
    String postID;

    public PostFiles(String imagePath, String imageThumbPath, String postID) {
        this.imagePath = imagePath;
        this.imageThumbPath = imageThumbPath;
        this.postID = postID;
    }

    public PostFiles() {
    }

    public String getImagePath() {

        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageThumbPath() {
        return imageThumbPath;
    }

    public void setImageThumbPath(String imageThumbPath) {
        this.imageThumbPath = imageThumbPath;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }
}
