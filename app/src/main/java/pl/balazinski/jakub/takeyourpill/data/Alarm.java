package pl.balazinski.jakub.takeyourpill.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Kuba on 2016-01-31.
 */
@DatabaseTable(tableName = "alarm")
public class Alarm {

    @DatabaseField(generatedId = true, columnName = "id")
    private Long mId;

    //private Date date;
  //  @DatabaseField
  //  private Calendar time;

    @DatabaseField
    private int hour;

    @DatabaseField
    private int minute;

    @DatabaseField
    private boolean isActive;

    @DatabaseField
    private Long pillId;

    /*@ForeignCollectionField(eager = false)
    private ForeignCollection<Long> pillIdList;*/

    public Alarm() {
    }

    public Alarm(int hour, int minute, Long pill, boolean isActive) {
        this.pillId = pill;
        this.isActive = isActive;
        this.hour = hour;
        this.minute = minute;
    }



    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

  /*  public List<Long> getPillIdList() {
        return pillIdList;
    }

    public void setPillIdList(List<Long> pillIdList) {
        this.pillIdList = pillIdList;
    }*/

    public Long getId() {
        return mId;
    }

    public void setId(Long mId) {
        this.mId = mId;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
