package com.lastterm.finalexam.ui.fragments.contract;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Contract;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.TenantContractAdapter;

import java.util.List;

public class ContractFragment extends Fragment{
    RecyclerView recyclerView;
    Toolbar toolbar;

    List<Contract> contractList;
    RoomRepository repository;

    TenantContractAdapter adapter;

    public ContractFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contract_management, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.contract_management);

        repository = new RoomRepository();
        loadContracts();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void loadContracts() {
        repository.getContract((contracts) -> {
            contractList = contracts;
            adapter = new TenantContractAdapter(getContext(), contractList);
            recyclerView.setAdapter(adapter);
        });
    }

}
