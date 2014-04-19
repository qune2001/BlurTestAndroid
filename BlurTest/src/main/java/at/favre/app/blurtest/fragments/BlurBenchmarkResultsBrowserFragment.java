package at.favre.app.blurtest.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.inqbarna.tablefixheaders.TableFixHeaders;

import java.util.TreeSet;

import at.favre.app.blurtest.R;
import at.favre.app.blurtest.activities.MainActivity;
import at.favre.app.blurtest.adapter.ResultTableAdapter;
import at.favre.app.blurtest.models.BenchmarkResultDatabase;
import at.favre.app.blurtest.util.BenchmarkUtil;
import at.favre.app.blurtest.util.BlurUtil;
import at.favre.app.blurtest.util.JsonUtil;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class BlurBenchmarkResultsBrowserFragment extends Fragment {

	private TableFixHeaders table;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.results_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteData() {
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        settings.edit().putString(MainActivity.PREF_RESULTS,JsonUtil.toJsonString(new BenchmarkResultDatabase())).commit();
        table.setAdapter(new ResultTableAdapter(getActivity(),loadResultsDB()));
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_resultbrowser,container,false);
		table = (TableFixHeaders) v.findViewById(R.id.table);
		table.setAdapter(new ResultTableAdapter(getActivity(),loadResultsDB()));
		return v;
	}

	private void fillResults(View rootView) {
		BenchmarkResultDatabase resultsDB = loadResultsDB();

		if(resultsDB != null) {
			TableLayout tl = (TableLayout) rootView.findViewById(R.id.table);
			TreeSet<String> rowHeaders = new TreeSet<String>();
			for (BenchmarkResultDatabase.BenchmarkEntry benchmarkEntry : resultsDB.getEntryList()) {
				rowHeaders.add(benchmarkEntry.getCategory());
			}

			TableRow th = new TableRow(getActivity());
			th.addView(createTextView("","",th));
			for (BlurUtil.Algorithm algorithm : BlurUtil.Algorithm.getAllAlgorithms()) {
				th.addView(createTextView(algorithm.toString(), algorithm.toString(), th));
			}
			th.setBackgroundColor(getResources().getColor(R.color.tableHeaderBg));
			tl.addView(th);


			for (String rowHeader : rowHeaders) {
				TableRow tr = new TableRow(getActivity());
				View v = createTextView(rowHeader,rowHeader,tr);
				v.setBackgroundColor(getResources().getColor(R.color.tableRowHeaderBg));
				tr.addView(v);

				for (BlurUtil.Algorithm algorithm : BlurUtil.Algorithm.getAllAlgorithms()) {
					BenchmarkResultDatabase.BenchmarkEntry entry = resultsDB.getByCategoryAndAlgorithm(rowHeader,algorithm);
					if(entry == null || entry.getWrapper().isEmpty()) {
						tr.addView(createTextView("?", "", tr));
					} else {
						tr.addView(createTextView(BenchmarkUtil.formatNum(entry.getWrapper().get(0).getStatInfo().getAsAvg().getAvg()), entry.getWrapper().get(0).getStatInfo().getKeyString(), tr));
					}

				}
				tl.addView(tr);
			}
		}
	}

	private BenchmarkResultDatabase loadResultsDB() {
		SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
		String resultsString = settings.getString(MainActivity.PREF_RESULTS,null);
		if(resultsString != null) {
			return JsonUtil.fromJsonString(resultsString, BenchmarkResultDatabase.class);
		} else {
			return null;
		}
	}

	private View createTextView(String text,String tag, TableRow tl) {
		FrameLayout layout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.inc_result_cell,tl,false);
		TextView tv = (TextView) layout.getChildAt(0);
		tv.setTag(tag);
		tv.setText(text);
		return layout;
	}
}
