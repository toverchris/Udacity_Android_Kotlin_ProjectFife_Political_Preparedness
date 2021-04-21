package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.graphics.alpha
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDao_Impl
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentLaunchBinding
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.android.synthetic.main.fragment_voter_info.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlin.math.absoluteValue

class VoterInfoFragment : Fragment() {

    @InternalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val _viewModel: VoterInfoViewModel by lazy {
            val activity = requireNotNull(this.activity) {
                "You can only access the viewModel after onActivityCreated()"
            }
            ViewModelProviders.of(this, VoterInfoViewModelFactory(activity.application))
                    .get(VoterInfoViewModel::class.java)
        }

        val binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

        val electionId = VoterInfoFragmentArgs.fromBundle(arguments!!).argElectionId
        val electionDivision = VoterInfoFragmentArgs.fromBundle(arguments!!).argDivision
        _viewModel.getVoterInfoFromApi(electionDivision,electionId)

        _viewModel.voterInfo.observeForever {
            val infoURL = _viewModel.voterInfo.value?.state?.get(0)?.electionAdministrationBody?.electionInfoUrl
            if (infoURL.isNullOrEmpty()) binding.stateLocations.visibility = View.INVISIBLE
            else binding.stateLocations.visibility = View.VISIBLE
            val ballotURL = _viewModel.voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
            if (ballotURL.isNullOrEmpty()) binding.stateBallot.visibility = View.INVISIBLE
            else binding.stateBallot.visibility = View.VISIBLE

            if(_viewModel.savedElectionsFromDatabase.value!!.contains(it.election)){
                binding.buttonFollowElection.text = _viewModel.updateButtonText("Follow election")
            }
        }

        binding.buttonFollowElection.setOnClickListener {
            if(button_followElection.text == "Follow election"){
                _viewModel.saveToDatabase()
                button_followElection.text = _viewModel.updateButtonText(button_followElection.text.toString())
            }else{
                _viewModel.removeFromDatabase()
                button_followElection.text = _viewModel.updateButtonText(button_followElection.text.toString())
            }
        }

        return binding.root
    }
}