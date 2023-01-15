/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
var EconomyService = Java.type('org.spongepowered.api.service.economy.EconomyService')
var Sponge = Java.type('org.spongepowered.api.Sponge')

function money(args) {
  if (player == null) return null

  var economyServiceOpt = Sponge.getServiceManager().provide(EconomyService.class)
  if (economyServiceOpt.isPresent()) {
    var economyService = economyServiceOpt.get()

    var targetOpt = economyService.getOrCreateAccount(player.getUniqueId())
    if (targetOpt.isPresent()) {
      var target = targetOpt.get()

      return target.getBalance(economyService.getDefaultCurrency())
    } else {
      throw new Error("Something went wrong. Can't find user.")
    }
  } else {
    throw new Error('There is no available economy service provider!')
  }
}
