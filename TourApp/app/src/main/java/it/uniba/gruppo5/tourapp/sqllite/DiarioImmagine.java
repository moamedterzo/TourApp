package it.uniba.gruppo5.tourapp.sqllite;

//Si occupa di contentere le informazioni di ogni immaginine durante la creazione dell activity_diario
public class DiarioImmagine {


    private String image_name_f;

    public DiarioImmagine(String img_perc){
        setImage_perc(img_perc);

    }


    public String getImage_perc() {
        return image_name_f;
    }

    public void setImage_perc(String android_image_url) {
        this.image_name_f = android_image_url;
    }
}