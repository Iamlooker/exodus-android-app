package org.eu.exodus_privacy.exodusprivacy.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.core.network.AndroidNetworkManager
import org.eu.exodus_privacy.exodusprivacy.core.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.core.packageInfo.AndroidPackageInfoManager
import org.eu.exodus_privacy.exodusprivacy.core.packageInfo.PackageInfoManager
import org.eu.exodus_privacy.exodusprivacy.data.remote.ExodusService
import org.eu.exodus_privacy.exodusprivacy.data.remote.RetrofitExodusService

@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {

    @Binds
    abstract fun bindExodusService(
        retrofitExodusService: RetrofitExodusService
    ) : ExodusService

    @Binds
    abstract fun bindNetworkManager(
        androidNetworkManager: AndroidNetworkManager
    ) : NetworkManager

    @Binds
    abstract fun bindPackageInfoManager(
        androidPackageInfoManager: AndroidPackageInfoManager
    ) : PackageInfoManager

}