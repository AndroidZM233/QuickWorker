package speedata.com.quickworker.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by 张明_ on 2017/7/31.
 */
@Entity
public class MyData {
    @Id(autoincrement = false)
    private String barCode;

    private String weight;
    private String volume;
    @Generated(hash = 544186294)
    public MyData(String barCode, String weight, String volume) {
        this.barCode = barCode;
        this.weight = weight;
        this.volume = volume;
    }
    @Generated(hash = 2083622869)
    public MyData() {
    }
    public String getBarCode() {
        return this.barCode;
    }
    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }
    public String getWeight() {
        return this.weight;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }
    public String getVolume() {
        return this.volume;
    }
    public void setVolume(String volume) {
        this.volume = volume;
    }
    
}
