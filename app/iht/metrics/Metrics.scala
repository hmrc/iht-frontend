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

import com.codahale.metrics.MetricRegistry
import iht.models.enums.KickOutSource._
import iht.models.enums.StatsSource.StatsSource
import iht.models.enums.{KickOutSource, _}
import uk.gov.hmrc.play.graphite.MicroserviceMetrics

/**
  *
  * Created by Vineet Tyagi on 29/09/15.
  *
  */

@Singleton
class Metrics extends MicroserviceMetrics {

  val registry: MetricRegistry = metrics.defaultRegistry

  val kickOutCounters = Map(
    KickOutSource.REGISTRATION-> registry.counter("registration-kickout-counter"),
    KickOutSource.ASSET-> registry.counter("asset-kickout-counter"),
    KickOutSource.GIFT-> registry.counter("gift-kickout-counter"),
    KickOutSource.TNRB-> registry.counter("tnrb-kickout-counter"),
    KickOutSource.EXEMPTIONS-> registry.counter("exemptions-kickout-counter"),
    KickOutSource.HOME-> registry.counter("home-kickout-counter")
  )

  val statsCounter = Map(
    StatsSource.COMPLETED_REG-> registry.counter("completedReg-counter"),
    StatsSource.COMPLETED_REG_ADDITIONAL_EXECUTORS-> registry.counter("completedReg-with-additional-executors-counter"),
    StatsSource.COMPLETED_APP-> registry.counter("completedApp-counter"),
    StatsSource.ADDITIONAL_EXECUTOR_APP-> registry.counter("with-additional-executor-counter"),
    StatsSource.NO_ASSETS_DEBTS_EXEMPTIONS_APP-> registry.counter("no-assets-debts-exemptions-app-counter"),
    StatsSource.ASSETS_ONLY_APP-> registry.counter("assets-anly-app-counter"),
    StatsSource.ASSETS_AND_DEBTS_ONLY_APP-> registry.counter("assets-and-debts-only-app-counter"),
    StatsSource.ASSET_DEBTS_EXEMPTIONS_TNRB_APP-> registry.counter("asset-debt-exemption-tnrb-app-counter")

  )

  def kickOutCounter(source: KickOutSource): Unit = kickOutCounters(source).inc()
  def generalStatsCounter (source: StatsSource):Unit = statsCounter(source).inc()
}
