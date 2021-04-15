package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.MainActivity
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
//import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.launch.LaunchFragmentDirections
import com.example.android.politicalpreparedness.network.models.Division
import kotlinx.coroutines.InternalCoroutinesApi

// TODO: 3/25/21 has been an abstract class before  
class ElectionsFragment: Fragment() {

    //TODO: Declare ViewModel
    @InternalCoroutinesApi
    private val _viewModel: ElectionsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, ElectionsViewModelFactory(activity.application))
                .get(ElectionsViewModel::class.java)
    }


    @InternalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //TODO: Add ViewModel values and create ViewModel

        //TODO: Add binding values
        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

        //TODO: Initiate recycler adapters
        binding.recyclerViewUpcomingElections.adapter = ElectionListAdapter(ElectionListAdapter.ElectionListener{
            Log.i("ElectionFragment", "new upcoming election name ${it.name}")
            _viewModel.displayElection(it)
        })

        binding.recyclerViewSavedElections.adapter = ElectionListAdapter(ElectionListAdapter.ElectionListener{
            Log.i("ElectionFragment", "new saved election name ${it.name}")
            _viewModel.displayElection(it)
        })

        _viewModel.navigateToSelectedElection.observe(viewLifecycleOwner, Observer {
            Log.i("ElectionsFragment", "observer triggered")
            if ( null != it ) {
                Log.i("ElectionsFragment", "navigate to election name ${it.name}")
                navToVoterInfo()
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                _viewModel.displayElectionDetailsComplete()
            } else {
                Log.i("ElectionsFragment", "is null")
            }
        })

        return binding.root
    }

    //TODO: Refresh adapters when fragment loads

    @InternalCoroutinesApi
    private fun navToVoterInfo() {
        this.findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(_viewModel.navigateToSelectedElection.value!!.id,_viewModel.navigateToSelectedElection.value!!.division ))
    }

    @InternalCoroutinesApi
    override fun onResume() {
        super.onResume()
        _viewModel.getElectionsDataFromDatabase()
    }
}