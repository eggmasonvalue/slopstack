package com.slopstack.dropslop

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** A stateless Quick Settings action that opens the focused Drop Slop popup. */
class DropSlopTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        val intent = Intent(this, DropSlopActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_DROP_SLOP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        startActivityAndCollapse(pendingIntent)
    }

    private companion object {
        const val REQUEST_OPEN_DROP_SLOP = 1
    }
}
