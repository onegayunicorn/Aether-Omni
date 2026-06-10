package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EscrowDao {
    @Query("SELECT * FROM escrow_contracts ORDER BY createdTimestamp DESC")
    fun getAllContractsFlow(): Flow<List<EscrowContract>>

    @Query("SELECT * FROM escrow_contracts WHERE contractId = :contractId LIMIT 1")
    suspend fun getContractById(contractId: String): EscrowContract?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: EscrowContract)

    @Update
    suspend fun updateContract(contract: EscrowContract)

    @Query("UPDATE escrow_contracts SET status = :status WHERE contractId = :contractId")
    suspend fun updateContractStatus(contractId: String, status: String)

    @Query("DELETE FROM escrow_contracts WHERE contractId = :contractId")
    suspend fun deleteContract(contractId: String)
}
