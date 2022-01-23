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
var Cause = Java.type('org.spongepowered.api.event.cause.Cause')
var EventContext = Java.type('org.spongepowered.api.event.cause.EventContext')
var EconomyService = Java.type('org.spongepowered.api.service.economy.EconomyService')
var Sponge = Java.type('org.spongepowered.api.Sponge')
var BigDecimal = Java.type('java.math.BigDecimal')

function MONEY(args) {
  if (player === null) return null

  if (args.length != 1 || typeof args[0] !== 'number')
    throw new Error('Invalid parameter! [Number]')

  var economyServiceOpt = Sponge.getServiceManager().provide(EconomyService.class)
  if (economyServiceOpt.isPresent()) {
    var economyService = economyServiceOpt.get()

    var targetOpt = economyService.getOrCreateAccount(player.getUniqueId())
    if (targetOpt.isPresent()) {
      var target = targetOpt.get()

      if (args[0] >= 0)
        target.deposit(
          economyService.getDefaultCurrency(),
          new BigDecimal(args[0]),
          Cause.of(EventContext.builder().build(), plugin)
        )
      else
        target.withdraw(
          economyService.getDefaultCurrency(),
          new BigDecimal(-args[0]),
          Cause.of(EventContext.builder().build(), plugin)
        )
    } else {
      throw new Error("Something went wrong. Can't find user.")
    }
  } else {
    throw new Error('There is no available economy service provider!')
  }
}
