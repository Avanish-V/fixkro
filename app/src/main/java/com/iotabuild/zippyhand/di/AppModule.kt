package com.iotabuild.zippyhand.di

import com.google.firebase.auth.FirebaseAuth
import com.iotabuild.zippyhand.AppSatartUp.AppStartUpViewModel
import com.iotabuild.zippyhand.feature.address.data.AddressRepositoryImpl
import com.iotabuild.zippyhand.feature.address.domain.AddressRepository
import com.iotabuild.zippyhand.feature.address.presentation.AddressViewModel
import com.iotabuild.zippyhand.feature.booking.data.BookingRepositoryImpl
import com.iotabuild.zippyhand.feature.booking.domain.BookingRepository
import com.iotabuild.zippyhand.feature.booking.domain.PricingRepository
import com.iotabuild.zippyhand.feature.booking.presentation.BookingDetailViewModel
import com.iotabuild.zippyhand.feature.booking.presentation.BookingSummaryViewModel
import com.iotabuild.zippyhand.feature.booking.presentation.BookingViewModel
import com.iotabuild.zippyhand.feature.home.presentation.HomeViewModel
import com.iotabuild.zippyhand.feature.offers.data.OfferRepositoryImpl
import com.iotabuild.zippyhand.feature.offers.domain.OfferRepository
import com.iotabuild.zippyhand.feature.offers.presentation.OffersViewModel
import com.iotabuild.zippyhand.feature.onboarding.data.CityRepositoryImpl
import com.iotabuild.zippyhand.feature.onboarding.domain.CityRepository
import com.iotabuild.zippyhand.feature.onboarding.presentation.CityViewModel
import com.iotabuild.zippyhand.feature.payments.data.PaymentRepositoryImpl
import com.iotabuild.zippyhand.feature.payments.domain.PaymentRepository
import com.iotabuild.zippyhand.feature.payments.presentation.PaymentViewModel
import com.iotabuild.zippyhand.feature.profile.data.AuthRepositoryImpl
import com.iotabuild.zippyhand.feature.profile.domain.AuthRepository
import com.iotabuild.zippyhand.feature.profile.presentation.AuthViewModel
import com.iotabuild.zippyhand.feature.reviews.data.ReviewRepositoryImpl
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewRepository
import com.iotabuild.zippyhand.feature.reviews.presentation.WriteReviewViewModel
import com.iotabuild.zippyhand.feature.servicecategory.data.CategoryRepositoryImpl
import com.iotabuild.zippyhand.feature.servicecategory.domain.CategoryRepository
import com.iotabuild.zippyhand.feature.servicecategory.presentation.CategoryViewModel
import com.iotabuild.zippyhand.feature.servicedetails.data.ServiceRepositoryImpl
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.feature.servicedetails.presentation.ServiceDetailViewModel
import com.iotabuild.zippyhand.feature.servicedetails.usecase.GetServiceDetailsUseCase
import androidx.room.Room
import com.iotabuild.zippyhand.feature.home.data.remote.HomeRepoImpl
import com.iotabuild.zippyhand.feature.home.domain.HomeRepository
import com.iotabuild.zippyhand.location.domain.LocationRepository
import com.iotabuild.zippyhand.location.data.local.AppDatabase
import com.iotabuild.zippyhand.location.data.remote.LocationRepoImpl
import com.iotabuild.zippyhand.location.presentation.LocationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "zippyhand_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().locationDao() }
    single { get<AppDatabase>().homeDao() }
    single { get<AppDatabase>().userDao() }
    single<LocationRepository> { LocationRepoImpl(androidContext(), get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    single { PricingRepository(get()) }
    single<AddressRepository> { AddressRepositoryImpl(get()) }
    single<PaymentRepository> { PaymentRepositoryImpl(get()) }
    single<BookingRepository> { BookingRepositoryImpl(get()) }
    single<OfferRepository> { OfferRepositoryImpl(get()) }
    single<ReviewRepository> { ReviewRepositoryImpl(get()) }
    single<CityRepository> { CityRepositoryImpl(get()) }
    single <HomeRepository>{ HomeRepoImpl(get(), get()) }
    factory { GetServiceDetailsUseCase(get(), get()) }
    
    viewModel { AuthViewModel(get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { HomeViewModel(get(), get(),get()) }
    viewModel { ServiceDetailViewModel(get()) }
    viewModel { BookingSummaryViewModel(get<ServiceRepository>(), get<PricingRepository>(), get<AddressRepository>(), get<OfferRepository>(), get<FirebaseAuth>()) }
    viewModel { AddressViewModel(get(), get()) }
    viewModel { PaymentViewModel(get(), get()) }
    viewModel { BookingViewModel(get()) }
    viewModel { BookingDetailViewModel(get()) }
    viewModel { OffersViewModel(get()) }
    viewModel { WriteReviewViewModel(get()) }
    viewModel { CityViewModel(get()) }
    viewModel { LocationViewModel(get()) }
    single { AppStartUpViewModel(get(), get()) }
}
