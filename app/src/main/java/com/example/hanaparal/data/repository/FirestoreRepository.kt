package com.example.hanaparal.data.repository

import android.util.Log
import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudentProfile
import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    private val studentsCollection = db.collection("students")
    private val studyGroupsCollection = db.collection("studyGroups")

    suspend fun createOrUpdateStudentProfile(profile: StudentProfile): Result<Unit> = runCatching {
        studentsCollection.document(profile.id).set(profile, SetOptions.merge()).await()
    }

    suspend fun updateFcmToken(userId: String, token: String): Result<Unit> = runCatching {
        studentsCollection.document(userId).update("fcmToken", token).await()
    }

    suspend fun getStudentProfile(userId: String): Result<StudentProfile?> = runCatching {
        val doc = studentsCollection.document(userId).get().await()
        doc.toObject(StudentProfile::class.java)?.copy(id = doc.id)
    }

    suspend fun createStudyGroup(group: StudyGroup): Result<String> = runCatching {
        val docRef = studyGroupsCollection.document()
        val groupWithId = group.copy(
            id = docRef.id,
            memberIds = listOf(group.adminId),
            memberCount = 1
        )
        docRef.set(groupWithId).await()
        docRef.collection("members").document(group.adminId).set(
            GroupMember(
                id = group.adminId,
                userId = group.adminId,
                userName = group.adminName,
                userEmail = group.adminEmail,
                isAdmin = true
            )
        ).await()
        docRef.id
    }

    fun getAllStudyGroups(): Flow<List<StudyGroup>> = callbackFlow {
        val listener = studyGroupsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val groups = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(StudyGroup::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(groups)
        }
        awaitClose { listener.remove() }
    }

    suspend fun requestToJoinGroup(groupId: String, userId: String): Result<Unit> = runCatching {
        val groupRef = studyGroupsCollection.document(groupId)
        val groupDoc = groupRef.get().await()
        val group = groupDoc.toObject(StudyGroup::class.java) ?: throw Exception("Group not found")

        if (group.memberIds.contains(userId)) throw Exception("Already a member")
        if (group.pendingMemberIds.contains(userId)) throw Exception("Request already sent")

        val maxMembers = RemoteConfigRepository.get().getConfig().maxMembersPerGroup
        if (group.memberCount >= maxMembers) throw Exception("Group is full")

        groupRef.update("pendingMemberIds", group.pendingMemberIds + userId).await()
    }

    suspend fun approveMember(groupId: String, member: GroupMember): Result<Unit> = runCatching {
        val groupRef = studyGroupsCollection.document(groupId)
        db.runTransaction { transaction ->
            val groupDoc = transaction.get(groupRef)
            val group = groupDoc.toObject(StudyGroup::class.java) ?: throw Exception("Group not found")

            transaction.set(groupRef.collection("members").document(member.userId), member)
            transaction.update(groupRef, mapOf(
                "memberIds" to (group.memberIds + member.userId),
                "pendingMemberIds" to (group.pendingMemberIds - member.userId),
                "memberCount" to (group.memberCount + 1)
            ))
        }.await()
    }

    suspend fun getPendingMembers(groupId: String): List<StudentProfile> {
        return try {
            val groupDoc = studyGroupsCollection.document(groupId).get().await()
            val pendingIds = groupDoc.toObject(StudyGroup::class.java)?.pendingMemberIds?.filter { it.isNotBlank() } ?: emptyList()

            val members = mutableListOf<StudentProfile>()
            for (id in pendingIds) {
                try {
                    val studentDoc = studentsCollection.document(id).get().await()
                    studentDoc.toObject(StudentProfile::class.java)?.let {
                        members.add(it.copy(id = id))
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Error fetching student profile for $id: ${e.message}")
                }
            }
            members
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching pending members: ${e.message}")
            emptyList()
        }
    }

    suspend fun getStudyGroup(groupId: String): Result<StudyGroup?> = runCatching {
        val doc = studyGroupsCollection.document(groupId).get().await()
        doc.toObject(StudyGroup::class.java)?.copy(id = doc.id)
    }



    suspend fun updateAnnouncement(groupId: String, announcement: String): Result<Unit> = runCatching {
        studyGroupsCollection.document(groupId).update("announcement", announcement).await()
    }

    suspend fun updateReminder(groupId: String, reminder: String): Result<Unit> = runCatching {
        studyGroupsCollection.document(groupId).update("reminder", reminder).await()
    }
}
