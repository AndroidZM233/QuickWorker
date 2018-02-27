package speedata.com.quickworker;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.serialport.SerialPort;
import android.util.Log;

import com.speedata.libutils.DataConversionUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import speedata.com.quickworker.bean.DaoMaster;
import speedata.com.quickworker.bean.DaoSession;


/**
 * Created by 张明_ on 2017/7/31.
 */

public class MyApp extends Application {
    private static MyApp m_application; // 单例
    //greendao
    private static DaoSession daoSession;
    private SerialPort serialPort = null;
    private ReadThread readThread;

    private void setupDatabase() {
        //创建数据库
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "QW.db", null);
        //获得可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获得数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获得dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoInstant() {
        return daoSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_application = this;
        setupDatabase();
        try {
            serialPort = new SerialPort();
            serialPort.OpenSerial("/dev/ttyMT1", 9600);
            readThread = new ReadThread();
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static MyApp getInstance() {
        return m_application;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        serialPort.CloseSerial(serialPort.getFd());
        readThread.interrupt();
    }

    class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                Log.i("aaa", "run: ");
                try {
                    int fd = serialPort.getFd();
                    if (fd == -1) {
                        EventBus.getDefault().post(new MsgEvent("msg", "请检测串口称连接是否断开"));
                        readThread.interrupt();
                        return;
                    }
                    byte[] bytes = serialPort.ReadSerial(fd, 16);
                    byte[] resultBytes = new byte[8];
                    for (int i = 0; i < bytes.length; i++) {
                        if (bytes[i] == 61) {
                            System.arraycopy(bytes, i, resultBytes, 0, 8);
                            break;
                        }
                    }
                    Log.i("aaa", "run: " + bytes);
                    if (resultBytes != null) {
                        EventBus.getDefault().post(new MsgEvent("weight",
                                DataConversionUtils.byteArrayToAscii(resultBytes)));
                    }else {
                        EventBus.getDefault().post(new MsgEvent("weightFailed",""));
                        serialPort.CloseSerial(serialPort.getFd());
                        readThread.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new MsgEvent("weightFailed",""));
                    serialPort.CloseSerial(serialPort.getFd());
                    readThread.interrupt();
                }
            }
        }
    }
}
