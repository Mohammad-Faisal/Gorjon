package candor.fulki.chat.meeting;


import java.io.Serializable;

/**
 * Created by Mohammad Faisal on 2/7/2018.
 */

public class MeetingRooms implements Serializable {
    private String title;
    private String type;
    private String details;
    private String tag;
    private String moderator;
    private String moderator_id;
    private String meeting_id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getModerator_id() {
        return moderator_id;
    }

    public void setModerator_id(String moderator_id) {
        this.moderator_id = moderator_id;
    }

    public String getMeeting_id() {
        return meeting_id;
    }

    public void setMeeting_id(String meeting_id) {
        this.meeting_id = meeting_id;
    }

    public String getNumber_of_person() {
        return number_of_person;
    }

    public void setNumber_of_person(String number_of_person) {
        this.number_of_person = number_of_person;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    private String number_of_person;

    public MeetingRooms(String title, String type, String details, String tag, String moderator, String moderator_id, String meeting_id, String number_of_person, String image_url) {
        this.title = title;
        this.type = type;
        this.details = details;
        this.tag = tag;
        this.moderator = moderator;
        this.moderator_id = moderator_id;
        this.meeting_id = meeting_id;
        this.number_of_person = number_of_person;
        this.image_url = image_url;
    }

    public MeetingRooms() {

    }
    private String image_url;

}
