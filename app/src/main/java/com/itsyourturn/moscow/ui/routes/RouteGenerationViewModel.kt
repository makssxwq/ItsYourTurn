package com.itsyourturn.moscow.ui.routes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.itsyourturn.moscow.data.model.Route
import com.itsyourturn.moscow.data.repository.RouteRepository
import com.itsyourturn.moscow.data.service.RouteGenerationService
import kotlinx.coroutines.launch

class RouteGenerationViewModel(
    private val routeGenerationService: RouteGenerationService,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _routeState = MutableLiveData<RouteState>()
    val routeState: LiveData<RouteState> = _routeState

    fun generateRoute(
        startPoint: GeoPoint,
        duration: Int,
        placeTypes: List<String>
    ) {
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            
            when (val result = routeGenerationService.generateRoute(
                startPoint = startPoint,
                duration = duration,
                preferredPlaceTypes = placeTypes
            )) {
                is RouteGenerationService.RouteGenerationResult.Success -> {
                    // Save the generated route
                    routeRepository.saveRoute(result.route)
                    _routeState.value = RouteState.Success(result.route)
                }
                is RouteGenerationService.RouteGenerationResult.Error -> {
                    _routeState.value = RouteState.Error(result.message)
                }
            }
        }
    }

    fun saveRouteAsFavorite(route: Route) {
        viewModelScope.launch {
            try {
                routeRepository.saveRoute(route)
                _routeState.value = RouteState.RouteSaved
            } catch (e: Exception) {
                _routeState.value = RouteState.Error("Failed to save route: ${e.message}")
            }
        }
    }
}

sealed class RouteState {
    object Loading : RouteState()
    data class Success(val route: Route) : RouteState()
    data class Error(val message: String) : RouteState()
    object RouteSaved : RouteState()
}
