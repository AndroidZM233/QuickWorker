package speedata.com.quickworker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import speedata.com.quickworker.bean.MyData;
import xyz.reginer.baseadapter.CommonRvAdapter;

public class MainActivity extends Activity implements CommonRvAdapter.OnItemClickListener {

    private EditText et_barcode;
    private RecyclerView rv_content;
    private RVAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private List<MyData> myDatas;
    private TextView tv_weight;
    private TextView tv_volume;
    private volatile String oldWeight="0001.00";
    private volatile int count=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        initView();
        myDatas = MyApp.getDaoInstant().getMyDataDao().loadAll();
        initRV();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mainEvent(MsgEvent msgEvent){
        Object msg = msgEvent.getMsg();
        String type = msgEvent.getType();
        if (type.equals("msg")){
            Toast.makeText(MainActivity.this, (String) msg, Toast.LENGTH_LONG).show();
        }else if (type.equals("weight")){
            StringBuffer stringBuffer = new StringBuffer((String) msg);
            String weight = stringBuffer.reverse().toString().replace("=", "");
            if (!weight.equals("0000.00")){
                if (oldWeight.equals(weight)){
                    count++;
                }else {
                    count=0;
                }
                oldWeight=weight;
            }
            if (count>8){
                tv_weight.setBackgroundColor(getResources().getColor(R.color.green));
            }else {
                tv_weight.setBackgroundColor(getResources().getColor(R.color.red));
            }
            tv_weight.setText(weight);
        }else if (type.equals("update")){
            myDatas.clear();
            myDatas.addAll(MyApp.getDaoInstant().getMyDataDao().loadAll());
            mAdapter.notifyDataSetChanged();
        }else if (type.equals("weightFailed")){
            tv_weight.setBackgroundColor(getResources().getColor(R.color.red));
            tv_weight.setText("称连接断开");
        }
    }

    private void initView() {
        et_barcode = (EditText) findViewById(R.id.et_barcode);
        rv_content = (RecyclerView) findViewById(R.id.rv_content);
        et_barcode.addTextChangedListener(new EditChangedListener());
        tv_weight = (TextView) findViewById(R.id.tv_weight);
        tv_volume = (TextView) findViewById(R.id.tv_volume);
    }

    private void initRV() {
        mAdapter = new RVAdapter(this, R.layout.info_show, myDatas);
        rv_content.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
        layoutManager.setReverseLayout(true);//列表翻转
        rv_content.setLayoutManager(layoutManager);
        rv_content.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, View view, int position) {

    }

    class EditChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int indexOf = s.toString().indexOf("\n");
            if (indexOf != -1) {
                String replace = s.toString().replace("\n", "");
                Toast.makeText(MainActivity.this, replace, Toast.LENGTH_LONG).show();
                ReadThread readThread = new ReadThread(replace);
                readThread.start();
                et_barcode.setText("");
            }
        }
    }

    class ReadThread extends Thread {
        private String barcode;

        public ReadThread(String barcode) {
            this.barcode = barcode;
        }

        @Override
        public void run() {
            super.run();
                String toString = tv_weight.getText().toString();
                if (!TextUtils.isEmpty(toString)) {
                    MyData myData = new MyData();
                    myData.setBarCode(barcode);
                    myData.setWeight(Double.parseDouble(toString) + "");
                    myData.setVolume("0");
                    MyApp.getDaoInstant().getMyDataDao().insertOrReplace(myData);
                    EventBus.getDefault().post(new MsgEvent("update",""));
                } else {
                    handler.sendMessage(handler.obtainMessage(1, "请检测串口称连接是否断开"));
                }

        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(MainActivity.this, (String) msg.obj+" "+myDatas.size(), Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
