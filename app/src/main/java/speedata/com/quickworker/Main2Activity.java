package speedata.com.quickworker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.SerialPort;
import android.util.Log;
import android.widget.TextView;

import com.speedata.libutils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Main2Activity extends Activity {
    SerialPort serialPort;
    TextView textView;
    private ReadThread readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv_msg);

        try {
            serialPort = new SerialPort();
            serialPort.OpenSerial("/dev/ttyUSB0", 9600);
        } catch (IOException e) {
            e.printStackTrace();
        }

        readThread = new ReadThread();
        readThread.start();
    }

    class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                Log.i("aaa", "run: ");
                try {
                    int fd = serialPort.getFd();
                    byte[] bytes = serialPort.ReadSerial(fd, 8);
                    isDataStabilization(bytes);
                    Log.i("aaa", "run: " + bytes);
                    if (bytes != null) {
                        handler.sendMessage(handler.obtainMessage(0, bytes));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void isDataStabilization(byte[] b) {


    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("aaa", "handleMessage: " + DataConversionUtils.byteArrayToAscii((byte[]) msg.obj));
            textView.setText(DataConversionUtils.byteArrayToAscii((byte[]) msg.obj) + "\n");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPort.CloseSerial(serialPort.getFd());
        readThread.interrupt();
    }
}
