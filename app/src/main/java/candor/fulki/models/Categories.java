package candor.fulki.models;

public class Categories {
    private String name;
    private boolean selected;

    public Categories(String name  , boolean selected){
        this.name = name;
        this.selected= selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
    }
}


