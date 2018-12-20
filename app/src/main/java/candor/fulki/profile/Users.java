package candor.fulki.profile;

/**
 * Created by Mohammad Faisal on 2/5/2018.
 */

public class Users {


    String  name;
    String user_name;
    String user_id;
    String bio;
    String gender;
    String profession;

    String division;
    String district;

    String blood_group;
    String birth_date;
    String contact_no;
    String image;
    String thumb_image;


    public Users() {
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getBlood_group() {
        return blood_group;
    }

    public void setBlood_group(String blood_group) {
        this.blood_group = blood_group;
    }

    public String getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    public String getContact_no() {
        return contact_no;
    }

    public void setContact_no(String contact_no) {
        this.contact_no = contact_no;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public Users(String name, String user_name, String user_id, String bio, String gender, String profession, String division, String district, String blood_group, String birth_date, String contact_no, String image, String thumb_image) {

        this.name = name;
        this.user_name = user_name;
        this.user_id = user_id;
        this.bio = bio;
        this.gender = gender;
        this.profession = profession;
        this.division = division;
        this.district = district;
        this.blood_group = blood_group;
        this.birth_date = birth_date;
        this.contact_no = contact_no;
        this.image = image;
        this.thumb_image = thumb_image;
    }
}
