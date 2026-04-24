/* global Chart, SockJS, Stomp */
(function () {
  const chartRefs = {};

  function destroyChart(key) {
    if (chartRefs[key]) {
      chartRefs[key].destroy();
      delete chartRefs[key];
    }
  }

  function destroyAllUserCharts() {
    Object.keys(chartRefs).forEach(destroyChart);
  }

  const colors = {
    primary: "#6c5ce7",
    secondary: "#00b894",
    accent: "#fdcb6e",
    danger: "#e17055",
    muted: "#b2bec3",
    grid: "rgba(0,0,0,0.06)",
  };

  function renderUserAnalytics(data, prefix) {
    if (!data || typeof Chart === "undefined") return;

    const p = prefix || "";

    const cgpaLineEl = document.getElementById(p + "chartCgpaLine");
    const eduTrendsWrap = document.getElementById(p + "eduTrendCharts");
    const skillsEl = document.getElementById(p + "chartSkillsDoughnut");
    const plBarEl = document.getElementById(p + "chartPlBar");
    const sectionsEl = document.getElementById(p + "chartSectionsBar");
    const radarEl = document.getElementById(p + "chartRadar");
    const percentileEl = document.getElementById(p + "chartPercentileBar");
    const insightsEl = document.getElementById(p + "analyticsInsights");
    const kpiEl = document.getElementById(p + "analyticsKpis");
    const cohortCompareEl = document.getElementById(p + "cohortCompareContent");

    if (kpiEl) {
      const pct = data.percentileCompare && data.percentileCompare.userScores ? data.percentileCompare.userScores : [];
      function pctOf(label) {
        const idx = (data.percentileCompare && data.percentileCompare.labels || []).indexOf(label);
        return idx >= 0 ? pct[idx] : null;
      }
      const profPct = pctOf("Profile");
      const acadPct = pctOf("Academic");
      const skillPct = pctOf("Skills");
      const expPct = pctOf("Experience");
      const credPct = pctOf("Credentials");
      const cards = [
        { t: "Profile score", v: (data.profileCompleteness != null ? data.profileCompleteness + "/100" : "—"), s: profPct != null ? Math.round(profPct) + "th percentile" : "" },
        { t: "Academic", v: (data.averageCgpa != null ? String(data.averageCgpa) : "—"), s: acadPct != null ? Math.round(acadPct) + "th percentile" : "" },
        { t: "Skills breadth", v: (data.skillCount != null ? data.skillCount : "—") + " skills", s: skillPct != null ? Math.round(skillPct) + "th percentile" : "" },
        { t: "Experience", v: (data.experienceCount != null ? data.experienceCount : "—") + " rows", s: expPct != null ? Math.round(expPct) + "th percentile" : "" },
        { t: "Credentials", v: ((data.certificateCount || 0) + (data.documentCount || 0)) + " files", s: credPct != null ? Math.round(credPct) + "th percentile" : "" },
      ];
      if (data.communicationScore != null) {
        cards.push({ t: "Communication", v: Number(data.communicationScore).toFixed(1) + "/100", s: "From interview transcript" });
      }
      if (data.technicalScore != null) {
        cards.push({ t: "Technical", v: Number(data.technicalScore).toFixed(1) + "/100", s: "From live coding" });
      }
      if (data.behavioralScore != null) {
        cards.push({ t: "Behavior", v: Number(data.behavioralScore).toFixed(1) + "/100", s: "Interview behavior" });
      }
      if (data.interviewFinalScore != null) {
        cards.push({ t: "Interview final", v: Number(data.interviewFinalScore).toFixed(1) + "/100", s: "Weighted interview score" });
      }
      kpiEl.innerHTML = cards.map(function (c) {
        return "<div style='background:#fff;border-radius:12px;padding:12px;box-shadow:0 1px 6px rgba(0,0,0,0.06)'>" +
          "<div style='font-size:12px;color:#636e72;font-weight:700;letter-spacing:0.2px'>" + escapeHtml(c.t) + "</div>" +
          "<div style='font-size:22px;font-weight:900;margin-top:6px;color:#2d3436;line-height:1.05'>" + escapeHtml(String(c.v)) + "</div>" +
          (c.s ? "<div style='font-size:12px;color:#6c5ce7;margin-top:8px;font-weight:800'>" + escapeHtml(c.s) + "</div>" : "") +
          "</div>";
      }).join("");
    }

    if (insightsEl && data.insights) {
      insightsEl.innerHTML =
        "<ul style='margin:8px 0;padding-left:20px;color:#444'>" +
        data.insights.map((t) => "<li>" + escapeHtml(t) + "</li>").join("") +
        "</ul>";
    }

    if (cohortCompareEl) {
      const pc = data.percentileCompare || {};
      const labels = pc.labels || [];
      const vals = pc.userScores || [];
      const ranked = labels.map(function (label, i) {
        return { label: label, value: Number(vals[i] || 0) };
      }).sort(function (a, b) { return a.value - b.value; });
      const weak = ranked.slice(0, 2);
      const strong = ranked.slice(-2).reverse();
      const suggestionMap = {
        Profile: "Complete missing profile fields, links, and profile photo.",
        Academic: "Add semester-wise marks/CGPA for all semesters.",
        Skills: "Add more skills and proper skill levels.",
        Experience: "Add internship/job experiences with dates and documents.",
        Credentials: "Upload certificates and important documents."
      };
      const weakHtml = weak.length
        ? weak.map(function (x) {
            return "<li><strong>" + escapeHtml(x.label) + "</strong> (" + Math.round(x.value) + "th percentile) - " + escapeHtml(suggestionMap[x.label] || "Improve this section for stronger profile rank.") + "</li>";
          }).join("")
        : "<li>No comparison data available.</li>";
      const strongHtml = strong.length
        ? strong.map(function (x) {
            return "<li><strong>" + escapeHtml(x.label) + "</strong> (" + Math.round(x.value) + "th percentile)</li>";
          }).join("")
        : "<li>No strong areas available yet.</li>";
      cohortCompareEl.innerHTML =
        "<div style='display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:16px'>" +
          "<div><div style='font-weight:700;color:#6c5ce7;margin-bottom:6px'>What you should improve</div><ul style='margin:0;padding-left:18px'>" + weakHtml + "</ul></div>" +
          "<div><div style='font-weight:700;color:#00b894;margin-bottom:6px'>Your strongest areas</div><ul style='margin:0;padding-left:18px'>" + strongHtml + "</ul></div>" +
        "</div>";
    }

    function renderTrendChart(canvasEl, trendPoints, titleText, keySuffix) {
      if (!canvasEl) return;
      const trend = trendPoints || [];
      destroyChart(p + "cgpa" + keySuffix);
      if (!trend.length) return;

      const labels = trend.map((x) => x.label || "S" + (x.semester || ""));
      const cgpaData = trend.map((x) => (x.cgpa != null ? x.cgpa : null));
      const pctData = trend.map((x) => (x.percentage != null ? x.percentage : null));
      const hasCgpa = cgpaData.some((v) => v != null);
      const hasPct = pctData.some((v) => v != null);
      const datasets = [];
      if (hasCgpa) {
        datasets.push({
          label: "CGPA",
          data: cgpaData,
          borderColor: colors.primary,
          backgroundColor: "rgba(108,92,231,0.15)",
          tension: 0.25,
          spanGaps: true,
          yAxisID: "y",
        });
      }
      if (hasPct) {
        datasets.push({
          label: "Marks %",
          data: pctData,
          borderColor: colors.secondary,
          backgroundColor: "rgba(0,184,148,0.12)",
          tension: 0.25,
          spanGaps: true,
          yAxisID: hasCgpa ? "y1" : "y",
        });
      }
      const scales = { y: { type: "linear", position: "left", grid: { color: colors.grid } } };
      if (hasCgpa && hasPct) {
        scales.y.title = { display: true, text: "CGPA" };
        scales.y.min = 0;
        scales.y.max = 10;
        scales.y1 = {
          type: "linear",
          position: "right",
          grid: { drawOnChartArea: false },
          title: { display: true, text: "Marks %" },
          min: 0,
          max: 100,
        };
      } else if (hasCgpa) {
        scales.y.title = { display: true, text: "CGPA" };
        scales.y.min = 0;
        scales.y.max = 10;
      } else {
        scales.y.title = { display: true, text: "Marks %" };
        scales.y.min = 0;
        scales.y.max = 100;
      }
      chartRefs[p + "cgpa" + keySuffix] = new Chart(canvasEl, {
        type: "line",
        data: { labels, datasets },
        options: {
          responsive: true,
          plugins: {
            legend: { position: "top" },
            title: { display: true, text: titleText || "Academic trend" },
          },
          scales,
        },
      });
    }

    const series = data.educationTrendsByLevel || [];
    if (eduTrendsWrap) {
      // clear old per-level charts
      Object.keys(chartRefs)
        .filter(function (k) { return k.indexOf(p + "cgpa:") === 0; })
        .forEach(destroyChart);
      destroyChart(p + "cgpa");

      eduTrendsWrap.innerHTML = "";
      const flatAny = (series || []).some(function (s) { return s && s.points && s.points.length; });
      if (!flatAny) {
        const hint =
          (eduTrendsWrap.parentElement && eduTrendsWrap.parentElement.querySelector(".chart-empty-hint")) ||
          null;
        if (hint) hint.style.display = "block";
      } else {
        const hint =
          (eduTrendsWrap.parentElement && eduTrendsWrap.parentElement.querySelector(".chart-empty-hint")) ||
          null;
        if (hint) hint.style.display = "none";

        (series || []).forEach(function (s, idx) {
          if (!s || !s.points || !s.points.length) return;
          const box = document.createElement("div");
          box.className = "edu-trend-box";
          const cv = document.createElement("canvas");
          cv.height = 180;
          box.appendChild(cv);
          eduTrendsWrap.appendChild(box);
          const title = (s.educationLevel ? s.educationLevel + " trend" : "Academic trend");
          renderTrendChart(cv, s.points, title, ":" + idx);
        });
      }
    } else {
      const trend = data.educationTrend || [];
      destroyChart(p + "cgpa");
      if (cgpaLineEl && trend.length) {
        renderTrendChart(cgpaLineEl, trend, "Academic trend (by semester row)", "");
      } else if (cgpaLineEl) {
        const hint =
          (cgpaLineEl.parentElement && cgpaLineEl.parentElement.querySelector(".chart-empty-hint")) ||
          null;
        if (hint) hint.style.display = "block";
      }
    }

    destroyChart(p + "skills");
    const sk = data.skillLevelDistribution || [];
    if (skillsEl && sk.length) {
      chartRefs[p + "skills"] = new Chart(skillsEl, {
        type: "doughnut",
        data: {
          labels: sk.map((x) => x.name),
          datasets: [
            {
              data: sk.map((x) => x.value),
              backgroundColor: [
                "#6c5ce7",
                "#00b894",
                "#fdcb6e",
                "#e17055",
                "#74b9ff",
                "#a29bfe",
              ],
            },
          ],
        },
        options: {
          responsive: true,
          plugins: {
            title: { display: true, text: "Skills by level" },
            legend: { position: "right" },
          },
        },
      });
    }

    destroyChart(p + "pl");
    const pl = data.programmingLanguages || [];
    if (plBarEl && pl.length) {
      chartRefs[p + "pl"] = new Chart(plBarEl, {
        type: "bar",
        data: {
          labels: pl.map((x) => x.name),
          datasets: [
            {
              label: "Listed",
              data: pl.map(() => 1),
              backgroundColor: colors.secondary,
            },
          ],
        },
        options: {
          indexAxis: "y",
          responsive: true,
          plugins: {
            title: { display: true, text: "Programming languages" },
            legend: { display: false },
          },
          scales: { x: { beginAtZero: true, max: 2, ticks: { stepSize: 1 } } },
        },
      });
    }

    destroyChart(p + "sections");
    const sec = data.sectionCounts || [];
    if (sectionsEl && sec.length) {
      chartRefs[p + "sections"] = new Chart(sectionsEl, {
        type: "bar",
        data: {
          labels: sec.map((x) => x.name),
          datasets: [
            {
              label: "Count",
              data: sec.map((x) => x.value),
              backgroundColor: [
                "#6c5ce7",
                "#00cec9",
                "#fdcb6e",
                "#e84393",
                "#0984e3",
                "#6ab04c",
              ],
            },
          ],
        },
        options: {
          responsive: true,
          plugins: {
            title: { display: true, text: "Profile depth (row counts)" },
            legend: { display: false },
          },
          scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } },
        },
      });
    }

    destroyChart(p + "radar");
    const rc = data.radarCompare;
    if (radarEl && rc && rc.labels && rc.userScores) {
      chartRefs[p + "radar"] = new Chart(radarEl, {
        type: "radar",
        data: {
          labels: rc.labels,
          datasets: [
            {
              label: "You",
              data: rc.userScores,
              borderColor: colors.primary,
              backgroundColor: "rgba(108,92,231,0.2)",
            },
            {
              label: "Cohort avg",
              data: rc.cohortScores || [],
              borderColor: colors.muted,
              backgroundColor: "rgba(178,190,195,0.15)",
            },
          ],
        },
        options: {
          responsive: true,
          plugins: {
            title: { display: true, text: "You vs all users (normalized scores)" },
          },
          scales: { r: { beginAtZero: true, max: 100 } },
        },
      });
    }

    destroyChart(p + "pct");
    const pc = data.percentileCompare;
    if (percentileEl && pc && pc.labels && pc.userScores) {
      chartRefs[p + "pct"] = new Chart(percentileEl, {
        type: "bar",
        data: {
          labels: pc.labels,
          datasets: [
            { label: "Your percentile", data: pc.userScores, backgroundColor: colors.primary },
            { label: "Median", data: (pc.labels || []).map(function () { return 50; }), backgroundColor: "rgba(178,190,195,0.6)" },
          ],
        },
        options: {
          responsive: true,
          plugins: {
            title: { display: true, text: "Your standing vs cohort (percentile)" },
          },
          scales: { y: { beginAtZero: true, max: 100 } },
        },
      });
    }
  }

  function escapeHtml(s) {
    if (!s) return "";
    const d = document.createElement("div");
    d.textContent = s;
    return d.innerHTML;
  }

  let userStomp = null;

  function connectUserAnalyticsWs(userId, prefix, onData) {
    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") return;
    try {
      if (userStomp) {
        userStomp.disconnect();
        userStomp = null;
      }
      const socket = new SockJS("/ws");
      const client = Stomp.over(socket);
      client.debug = null;
      client.connect(
        {},
        function () {
          userStomp = client;
          client.subscribe("/topic/analytics/user/" + userId, function (msg) {
            try {
              const data = JSON.parse(msg.body);
              onData(data);
            } catch (e) {}
          });
        },
        function () {}
      );
    } catch (e) {}
  }

  async function fetchUserAnalytics(userId) {
    const res = await fetch("/api/users/" + userId + "/profile/analytics");
    if (!res.ok) throw new Error("analytics failed");
    return res.json();
  }

  window.ProfileAnalyticsUI = {
    mountUserCharts: async function (userId, prefix) {
      if (!userId) return;
      destroyAllUserCharts();
      try {
        const data = await fetchUserAnalytics(userId);
        renderUserAnalytics(data, prefix || "");
        connectUserAnalyticsWs(userId, prefix || "", function (d) {
          renderUserAnalytics(d, prefix || "");
        });
      } catch (e) {
        console.warn("Profile analytics:", e);
      }
    },

    dispose: function () {
      destroyAllUserCharts();
      if (userStomp) {
        try {
          userStomp.disconnect();
        } catch (e) {}
        userStomp = null;
      }
    },

    initFromSession: function (prefix) {
      try {
        const u = JSON.parse(
          sessionStorage.getItem("user") || localStorage.getItem("user") || "null"
        );
        if (!u || u.id == null) return;
        const uid = Number(u.id);
        if (!Number.isFinite(uid) || uid <= 0) return;
        this.mountUserCharts(uid, prefix || "");
      } catch (e) {}
    },

    initAdminTalentPage: async function () {
      if (typeof Chart === "undefined") return;
      const sel = document.getElementById("talentUserSelect");
      const rankCanvas = document.getElementById("adminTalentRankBar");
      const radarCanvas = document.getElementById("adminTalentRadar");
      const compareCanvas = document.getElementById("adminTalentCompareBar");
      const scatterCanvas = document.getElementById("adminTalentScatter");
      const mapEl = document.getElementById("talentLocationMap");
      const summaryEl = document.getElementById("locationSummaryText");
      const countryFilter = document.getElementById("locationCountryFilter");
      const stateFilter = document.getElementById("locationStateFilter");
      const cityFilter = document.getElementById("locationCityFilter");
      const topCountriesList = document.getElementById("topCountriesList");
      const topStatesList = document.getElementById("topStatesList");
      const topCitiesList = document.getElementById("topCitiesList");
      if (!sel || !rankCanvas) return;

      let pool = { users: [] };
      let locationData = { points: [], countries: [], states: [], cities: [], totalLocatedUsers: 0 };
      let adminStomp = null;
      let map = null;
      let markerLayer = null;

      function setTopList(el, rows) {
        if (!el) return;
        const top = (rows || []).slice(0, 8);
        el.innerHTML = top.length
          ? top.map(function (r) {
            return "<li>" + escapeHtml(r.name || "Unknown") + " (" + (r.count || 0) + ")</li>";
          }).join("")
          : "<li>No data</li>";
      }

      function setupLocationFilters(points) {
        if (!countryFilter || !stateFilter || !cityFilter) return;
        const countries = Array.from(new Set((points || []).map(function (p) { return p.country || ""; }).filter(Boolean))).sort();
        const states = Array.from(new Set((points || []).map(function (p) { return p.state || ""; }).filter(Boolean))).sort();
        const cities = Array.from(new Set((points || []).map(function (p) { return p.city || ""; }).filter(Boolean))).sort();
        function fillSelect(el, defaultLabel, arr) {
          if (!el) return;
          el.innerHTML = "";
          const d = document.createElement("option");
          d.value = "";
          d.textContent = defaultLabel;
          el.appendChild(d);
          arr.forEach(function (x) {
            const o = document.createElement("option");
            o.value = x;
            o.textContent = x;
            el.appendChild(o);
          });
        }
        fillSelect(countryFilter, "All countries", countries);
        fillSelect(stateFilter, "All states", states);
        fillSelect(cityFilter, "All cities", cities);
      }

      function filteredPoints() {
        const points = locationData.points || [];
        const c = countryFilter && countryFilter.value ? countryFilter.value : "";
        const s = stateFilter && stateFilter.value ? stateFilter.value : "";
        const ci = cityFilter && cityFilter.value ? cityFilter.value : "";
        return points.filter(function (p) {
          return (!c || p.country === c) && (!s || p.state === s) && (!ci || p.city === ci);
        });
      }

      function renderMapPoints() {
        if (!mapEl || typeof L === "undefined") return;
        if (!map) {
          map = L.map(mapEl).setView([20.5937, 78.9629], 4); // India-centered default
          L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 18,
            attribution: "&copy; OpenStreetMap contributors",
          }).addTo(map);
          markerLayer = L.layerGroup().addTo(map);
        }
        markerLayer.clearLayers();
        const points = filteredPoints();
        let total = 0;
        const bounds = [];
        points.forEach(function (p) {
          total += p.count || 0;
          const marker = L.circleMarker([p.lat, p.lng], {
            radius: Math.max(6, Math.min(18, 5 + (p.count || 1) * 1.2)),
            color: "#6c5ce7",
            fillColor: "#6c5ce7",
            fillOpacity: 0.5,
            weight: 2,
          });
          const names = (p.usernames || []).slice(0, 8).map(escapeHtml).join(", ");
          marker.bindPopup(
            "<div><strong>" + escapeHtml((p.city || "City") + ", " + (p.state || "State") + ", " + (p.country || "Country")) + "</strong>" +
            "<div>Applicants: " + (p.count || 0) + "</div>" +
            (names ? "<div style='margin-top:4px;font-size:12px'>Users: " + names + "</div>" : "") +
            "</div>"
          );
          markerLayer.addLayer(marker);
          bounds.push([p.lat, p.lng]);
        });

        if (summaryEl) {
          summaryEl.textContent = "Showing " + points.length + " mapped locations and " + total + " applicants.";
        }
        if (bounds.length) {
          map.fitBounds(bounds, { padding: [24, 24], maxZoom: 8 });
        }
      }

      async function loadLocations() {
        try {
          const res = await fetch("/api/admin/talent/locations");
          if (!res.ok) return;
          locationData = await res.json();
          setTopList(topCountriesList, locationData.countries || []);
          setTopList(topStatesList, locationData.states || []);
          setTopList(topCitiesList, locationData.cities || []);
          setupLocationFilters(locationData.points || []);
          renderMapPoints();
        } catch (e) {}
      }

      async function loadPool() {
        const res = await fetch("/api/admin/talent/analytics");
        if (!res.ok) return;
        pool = await res.json();
        const users = pool.users || [];
        sel.innerHTML =
          '<option value="">— Select user —</option>' +
          users
            .map(function (u) {
              const label =
                (u.displayName && u.displayName.trim()) || u.username || "User " + u.userId;
              return (
                '<option value="' +
                u.userId +
                '">' +
                escapeHtml(label) +
                " (" +
                escapeHtml(u.username || "") +
                ")</option>"
              );
            })
            .join("");

        destroyChart("adminRank");
        const top = users.slice(0, 16);
        chartRefs.adminRank = new Chart(rankCanvas, {
          type: "bar",
          data: {
            labels: top.map(function (u) {
              return (u.displayName && u.displayName.trim()) || u.username || "#" + u.userId;
            }),
            datasets: [
              {
                label: "Final interview score",
                data: top.map(function (u) {
                  return u.interviewFinalScore != null ? u.interviewFinalScore : u.profileCompleteness;
                }),
                backgroundColor: colors.primary,
              },
            ],
          },
          options: {
            indexAxis: "y",
            responsive: true,
            plugins: {
              title: { display: true, text: "Top applicants by final score (fallback: profile)" },
            },
            scales: { x: { beginAtZero: true, max: 100 } },
          },
        });

        destroyChart("adminCgpa");
        if (compareCanvas) {
          const withCgpa = users.filter(function (u) {
            return u.averageCgpa != null;
          }).slice(0, 12);
          if (withCgpa.length) {
            chartRefs.adminCgpa = new Chart(compareCanvas, {
              type: "bar",
              data: {
                labels: withCgpa.map(function (u) {
                  return (u.displayName && u.displayName.trim()) || u.username || "#" + u.userId;
                }),
                datasets: [
                  {
                    label: "Avg CGPA",
                    data: withCgpa.map(function (u) {
                      return u.averageCgpa;
                    }),
                    backgroundColor: colors.secondary,
                  },
                ],
              },
              options: {
                indexAxis: "y",
                responsive: true,
                plugins: {
                  title: { display: true, text: "Average CGPA (where provided)" },
                },
                scales: { x: { beginAtZero: true, max: 10 } },
              },
            });
          }
        }

        destroyChart("adminScatter");
        if (scatterCanvas) {
          const pts = (users || []).filter(function (u) { return u.averageCgpa != null; }).slice(0, 60);
          if (pts.length) {
            chartRefs.adminScatter = new Chart(scatterCanvas, {
              type: "scatter",
              data: {
                datasets: [
                  {
                    label: "Applicants",
                    data: pts.map(function (u) {
                      return { x: u.profileCompleteness, y: u.averageCgpa, name: (u.displayName || u.username || "#" + u.userId) };
                    }),
                    backgroundColor: "rgba(108,92,231,0.65)",
                  },
                ],
              },
              options: {
                responsive: true,
                plugins: {
                  title: { display: true, text: "Completeness vs Avg CGPA" },
                  tooltip: {
                    callbacks: {
                      label: function (ctx) {
                        const p = ctx.raw || {};
                        return (p.name || "User") + ": " + "Score " + p.x + ", CGPA " + p.y;
                      },
                    },
                  },
                },
                scales: {
                  x: { beginAtZero: true, max: 100, title: { display: true, text: "Profile completeness" } },
                  y: { beginAtZero: true, max: 10, title: { display: true, text: "Avg CGPA" } },
                },
              },
            });
          }
        }
        await loadLocations();
      }

      async function loadSelectedRadar() {
        const id = parseInt(sel.value, 10);
        destroyChart("adminRadar");
        if (!id || !radarCanvas) return;
        try {
          const data = await fetchUserAnalytics(id);
          const rc = data.radarCompare;
          if (rc && rc.labels) {
            chartRefs.adminRadar = new Chart(radarCanvas, {
              type: "radar",
              data: {
                labels: rc.labels,
                datasets: [
                  {
                    label: (data.displayName || data.username || "User") + " vs cohort",
                    data: rc.userScores,
                    borderColor: colors.primary,
                    backgroundColor: "rgba(108,92,231,0.25)",
                  },
                  {
                    label: "Cohort average",
                    data: rc.cohortScores,
                    borderColor: colors.muted,
                    backgroundColor: "rgba(178,190,195,0.12)",
                  },
                ],
              },
              options: {
                responsive: true,
                plugins: {
                  title: {
                    display: true,
                    text: "Selected user vs cohort (same metrics as user dashboard)",
                  },
                },
                scales: { r: { beginAtZero: true, max: 100 } },
              },
            });
          }
        } catch (e) {}
      }

      await loadPool();

      sel.addEventListener("change", loadSelectedRadar);
      [countryFilter, stateFilter, cityFilter].forEach(function (el) {
        if (el) el.addEventListener("change", renderMapPoints);
      });

      if (typeof SockJS !== "undefined" && typeof Stomp !== "undefined") {
        try {
          const socket = new SockJS("/ws");
          adminStomp = Stomp.over(socket);
          adminStomp.debug = null;
          adminStomp.connect({}, function () {
            adminStomp.subscribe("/topic/analytics/talent-refresh", function () {
              loadPool().then(function () {
                if (sel.value) loadSelectedRadar();
              });
            });
          });
        } catch (e) {}
      }
    },
  };
})();
