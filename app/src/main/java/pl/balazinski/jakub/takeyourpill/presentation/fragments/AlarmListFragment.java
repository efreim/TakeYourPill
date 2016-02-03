package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.AlarmListAdapter;

/**
 * Created by Kuba on 2016-01-31.
 */
public class AlarmListFragment extends Fragment{

    private AlarmListAdapter alarmListAdapter;
    private RecyclerView rv;

    //@Bind(R.id.fab)

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rv = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);

        setupRecyclerView(rv);

        return rv;
    }


    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        //listAdapter = PillListAdapter.getInstance(getActivity());
        alarmListAdapter = new AlarmListAdapter(getActivity());
        recyclerView.setAdapter(alarmListAdapter);
        alarmListAdapter.notifyDataSetChanged();

    }


    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        if (alarmListAdapter != null)
            alarmListAdapter.notifyDataSetChanged();
    }
}