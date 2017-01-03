/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iht.metrics

import com.kenshoo.play.metrics.MetricsRegistry
import iht.models.enums.KickOutSource._
import iht.models.enums.StatsSource.StatsSource
import iht.models.enums.{KickOutSource, _}
/**
 *
 * Created by Vineet Tyagi on 29/09/15.
 *
 */
trait Metrics {

  def kickOutCounter (source: KickOutSource):Unit
  def generalStatsCounter (source: StatsSource):Unit
}

object Metrics extends Metrics {

  val kickOutCounters = Map(
    KickOutSource.REGISTRATION-> MetricsRegistry.defaultRegistry.counter("registration-kickout-counter"),
    KickOutSource.ASSET-> MetricsRegistry.defaultRegistry.counter("asset-kickout-counter"),
    KickOutSource.GIFT-> MetricsRegistry.defaultRegistry.counter("gift-kickout-counter"),
    KickOutSource.TNRB-> MetricsRegistry.defaultRegistry.counter("tnrb-kickout-counter"),
    KickOutSource.EXEMPTIONS-> MetricsRegistry.defaultRegistry.counter("exemptions-kickout-counter"),
    KickOutSource.HOME-> MetricsRegistry.defaultRegistry.counter("home-kickout-counter")
  )

 val statsCounter = Map(
   StatsSource.COMPLETED_REG-> MetricsRegistry.defaultRegistry.counter("completedReg-counter"),
   StatsSource.COMPLETED_REG_ADDITIONAL_EXECUTORS-> MetricsRegistry.defaultRegistry.counter("completedReg-with-additional-executors-counter"),
   StatsSource.COMPLETED_APP-> MetricsRegistry.defaultRegistry.counter("completedApp-counter"),
   StatsSource.ADDITIONAL_EXECUTOR_APP-> MetricsRegistry.defaultRegistry.counter("with-additional-executor-counter"),
   StatsSource.NO_ASSETS_DEBTS_EXEMPTIONS_APP-> MetricsRegistry.defaultRegistry.counter("no-assets-debts-exemptions-app-counter"),
   StatsSource.ASSETS_ONLY_APP-> MetricsRegistry.defaultRegistry.counter("assets-anly-app-counter"),
   StatsSource.ASSETS_AND_DEBTS_ONLY_APP-> MetricsRegistry.defaultRegistry.counter("assets-and-debts-only-app-counter"),
   StatsSource.ASSET_DEBTS_EXEMPTIONS_TNRB_APP-> MetricsRegistry.defaultRegistry.counter("asset-debt-exemption-tnrb-app-counter")

 )

  override def kickOutCounter(source: KickOutSource): Unit = kickOutCounters(source).inc()
  override  def generalStatsCounter (source: StatsSource):Unit = statsCounter(source).inc()
}
