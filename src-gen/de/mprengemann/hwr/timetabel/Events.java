package de.mprengemann.hwr.timetabel;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table EVENTS.
 */
public class Events {

  private Long id;
  /**
   * Not-null value.
   */
  private String uid;
  /**
   * Not-null value.
   */
  private String room;
  /**
   * Not-null value.
   */
  private String lecturer;
  /**
   * Not-null value.
   */
  private String type;
  private String fullDescription;
  private java.util.Date start;
  private long subjectId;
  private java.util.Date end;

  /**
   * Used to resolve relations
   */
  private transient DaoSession daoSession;

  /**
   * Used for active entity operations.
   */
  private transient EventsDao myDao;

  private Subjects subjects;
  private Long subjects__resolvedKey;


  // KEEP FIELDS - put your custom fields here
  // KEEP FIELDS END

  public Events() {
  }

  public Events(Long id) {
    this.id = id;
  }

  public Events(Long id, String uid, String room, String lecturer, String type, String fullDescription, java.util.Date start, long subjectId, java.util.Date end) {
    this.id = id;
    this.uid = uid;
    this.room = room;
    this.lecturer = lecturer;
    this.type = type;
    this.fullDescription = fullDescription;
    this.start = start;
    this.subjectId = subjectId;
    this.end = end;
  }

  /**
   * called by internal mechanisms, do not call yourself.
   */
  public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getEventsDao() : null;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Not-null value.
   */
  public String getUid() {
    return uid;
  }

  /**
   * Not-null value; ensure this value is available before it is saved to the database.
   */
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Not-null value.
   */
  public String getRoom() {
    return room;
  }

  /**
   * Not-null value; ensure this value is available before it is saved to the database.
   */
  public void setRoom(String room) {
    this.room = room;
  }

  /**
   * Not-null value.
   */
  public String getLecturer() {
    return lecturer;
  }

  /**
   * Not-null value; ensure this value is available before it is saved to the database.
   */
  public void setLecturer(String lecturer) {
    this.lecturer = lecturer;
  }

  /**
   * Not-null value.
   */
  public String getType() {
    return type;
  }

  /**
   * Not-null value; ensure this value is available before it is saved to the database.
   */
  public void setType(String type) {
    this.type = type;
  }

  public String getFullDescription() {
    return fullDescription;
  }

  public void setFullDescription(String fullDescription) {
    this.fullDescription = fullDescription;
  }

  public java.util.Date getStart() {
    return start;
  }

  public void setStart(java.util.Date start) {
    this.start = start;
  }

  public long getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(long subjectId) {
    this.subjectId = subjectId;
  }

  public java.util.Date getEnd() {
    return end;
  }

  public void setEnd(java.util.Date end) {
    this.end = end;
  }

  /**
   * To-one relationship, resolved on first access.
   */
  public Subjects getSubjects() {
    if (subjects__resolvedKey == null || !subjects__resolvedKey.equals(subjectId)) {
      if (daoSession == null) {
        throw new DaoException("Entity is detached from DAO context");
      }
      SubjectsDao targetDao = daoSession.getSubjectsDao();
      subjects = targetDao.load(subjectId);
      subjects__resolvedKey = subjectId;
    }
    return subjects;
  }

  public void setSubjects(Subjects subjects) {
    if (subjects == null) {
      throw new DaoException("To-one property 'subjectId' has not-null constraint; cannot set to-one to null");
    }
    this.subjects = subjects;
    subjectId = subjects.getId();
    subjects__resolvedKey = subjectId;
  }

  /**
   * Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context.
   */
  public void delete() {
    if (myDao == null) {
      throw new DaoException("Entity is detached from DAO context");
    }
    myDao.delete(this);
  }

  /**
   * Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context.
   */
  public void update() {
    if (myDao == null) {
      throw new DaoException("Entity is detached from DAO context");
    }
    myDao.update(this);
  }

  /**
   * Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context.
   */
  public void refresh() {
    if (myDao == null) {
      throw new DaoException("Entity is detached from DAO context");
    }
    myDao.refresh(this);
  }

  // KEEP METHODS - put your custom methods here

  @Override
  public String toString() {
    return "Events [id=" + id + ", uid=" + uid + ", room=" + room
        + ", lecturer=" + lecturer + ", type=" + type + ", start="
        + start + ", subjectId=" + subjectId + ", end=" + end
        + ", subjects=" + subjects + ", subjects__resolvedKey="
        + subjects__resolvedKey + "]";
  }
  // KEEP METHODS END

}
