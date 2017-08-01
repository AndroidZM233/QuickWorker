package speedata.com.quickworker;

import android.content.Context;

import java.util.List;

import speedata.com.quickworker.bean.MyData;
import xyz.reginer.baseadapter.BaseAdapterHelper;
import xyz.reginer.baseadapter.CommonRvAdapter;

/**
 * Created by 张明_ on 2017/7/14.
 */

public class RVAdapter extends CommonRvAdapter<MyData> {

    public RVAdapter(Context context, int layoutResId, List data) {
        super(context, layoutResId, data);
    }


    @Override
    public void convert(BaseAdapterHelper helper, MyData item, int position) {
        helper.setText(R.id.tv_barcode,item.getBarCode());
        helper.setText(R.id.tv_weight,item.getWeight());
        helper.setText(R.id.tv_volume,item.getVolume());
    }

}
