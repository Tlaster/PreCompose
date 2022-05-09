package moe.tlaster.common.util

import android.os.Parcel
import android.os.Parcelable

actual class MemoryStateHolder actual constructor(
    actual val value: Any?
) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) = Unit

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MemoryStateHolder> {
        override fun createFromParcel(parcel: Parcel): MemoryStateHolder {
            return MemoryStateHolder(null)
        }

        override fun newArray(size: Int): Array<MemoryStateHolder?> {
            return arrayOfNulls(size)
        }
    }
}
