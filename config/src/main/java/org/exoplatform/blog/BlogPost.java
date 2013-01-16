package org.exoplatform.blog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple representation of a Blog Post
 */
public class BlogPost {

  public static final String JSON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private DateFormat dateFormat = new SimpleDateFormat(JSON_DATE_FORMAT);

  private String title;

  private String owner;

  private Date publicationDate;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getPublicationDate() {
    return dateFormat.format(publicationDate);
  }

  public void setPublicationDate(String publicationDate) throws ParseException {
    this.publicationDate = dateFormat.parse(publicationDate);
  }

  void setPublicationDateByDate(Date publicationDate) throws ParseException {
    this.publicationDate = publicationDate;
  }
}
