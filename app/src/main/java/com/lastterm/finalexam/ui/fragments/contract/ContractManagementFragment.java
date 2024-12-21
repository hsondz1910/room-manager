package com.lastterm.finalexam.ui.fragments.contract;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.repositories.ContractRepository;
import com.lastterm.finalexam.ui.adapter.ContractManagementAdapter;
import com.lastterm.finalexam.data.entities.Contract;

import java.util.ArrayList;
import java.util.List;

public class ContractManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private ContractManagementAdapter contractManagementAdapter;
    private List<Contract> contractList;
    private FirebaseFirestore db;
    private String userId;
    private Button btnPostContract;
    private boolean allChecked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contract_management, container, false);
        setHasOptionsMenu(true);

        // Set up the toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setHomeButtonEnabled(false);

        // Initialize menu
        toolbar.inflateMenu(R.menu.menu_contract_management);

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contractList = new ArrayList<>();
        contractManagementAdapter = new ContractManagementAdapter(contractList, requireContext());
        recyclerView.setAdapter(contractManagementAdapter);

        // Initialize Firebase and user ID
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load contracts
        loadContracts();

        return view;
    }

    private void loadContracts() {
        ContractRepository contractRepository = new ContractRepository();
        contractRepository.fetchContractsByUserId(userId, new ContractRepository.Callback() {
            @Override
            public void onSuccess(List<Contract> contracts) {
                contractList.clear();
                contractList.addAll(contracts);
                contractManagementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_contract_management, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.check_all) {
            toggleSelectAllContracts();
            return true;
        } else if (item.getItemId() == R.id.delete_selected) {
            deleteSelectedContracts();
            return true;
        } else if (item.getItemId() == R.id.delete_all) {
            deleteAllContracts();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void toggleSelectAllContracts() {
        if (!allChecked) {
            for (Contract contract : contractList) {
                contract.setSelected(true);
            }
        } else {
            for (Contract contract : contractList) {
                contract.setSelected(false);
            }
        }
        allChecked = !allChecked;
        contractManagementAdapter.notifyDataSetChanged();
    }

    private void deleteContract(String contractId) {
        ContractRepository contractRepository = new ContractRepository();
        contractRepository.deleteContract(contractId, new ContractRepository.Callback() {
            @Override
            public void onSuccess(List<Contract> contracts) {
                // Remove contract from the list
                for (Contract contract : contractList) {
                    if (contract.getContractId().equals(contractId)) {
                        contractList.remove(contract);
                        break;
                    }
                }
                contractManagementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSelectedContracts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Do you want to delete selected contracts?");
        builder.setMessage("You will lose all the selected contracts.");
        builder.setPositiveButton("YES", (dialogInterface, which) -> {
            List<Contract> selectedContracts = new ArrayList<>();
            for (Contract contract : contractList) {
                if (!contract.isSelected()) {
                    selectedContracts.add(contract);
                }
            }
            contractList.clear();
            contractList.addAll(selectedContracts);
            contractManagementAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> {
            // Do nothing
        });
        builder.show();
    }

    private void deleteAllContracts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Do you want to delete all contracts?");
        builder.setMessage("You will lose all your data.");
        builder.setPositiveButton("YES", (dialog, which) -> {
            contractList.clear();
            contractManagementAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            // Do nothing
        });
        builder.show();
    }
}
