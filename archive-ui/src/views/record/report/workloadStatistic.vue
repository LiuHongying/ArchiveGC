<template>
  <DataLayout>
    <template v-slot:header>
      <el-form :inline="true">
        <el-form-item>
          <span>月度工作量统计</span>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="startDate"
            type="date"
            :placeholder="$t('application.startDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="endDate"
            type="date"
            :placeholder="$t('application.startDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleReport()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div
        :style="{
          position: 'relative',
          height: layout.height - startHeight + 'px',
        }"
      >
        <split-pane
          v-on:resize="onSplitResize"
          :min-percent="20"
          :default-percent="topPercent"
          split="horizontal"
        >
          <template slot="paneL">
            <el-table
              ref="mainTable"
              :data="tables.mainTable.data"
              border
              stripe
              size="mini"
              v-loading="loading"
              :height="
                ((layout.height - startHeight) * topPercent) / 100 -
                topbarHeight
              "
            >
              <el-table-column type="index" width="30" fixed></el-table-column>
              <el-table-column
                v-for="item in tables.mainTable.columns"
                :key="item.prop"
                v-bind="item"
              ></el-table-column>
            </el-table>
          </template>
          <template slot="paneR">
            <el-row>
            <el-form :inline="true">
              <el-form-item>
                <span>季度工作量统计</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleReportQuarter()">{{
                  $t("application.SearchData")
                }}</el-button>
              </el-form-item>
            </el-form>
            </el-row>
            <el-row>
            <el-table
              ref="sonTable"
              :data="tables.sonTable.data"
              border
              stripe
              size="mini"
              v-loading="loading"
              :height="
                ((layout.height - startHeight) * topPercent) / 100 -
                topbarHeight
              "
            >
              <el-table-column type="index" width="30" fixed> </el-table-column>
              <el-table-column
                v-for="item in tables.sonTable.columns"
                :key="item.prop"
                v-bind="item"
              >
              </el-table-column>
            </el-table>
            </el-row>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "workFlowMonth",
  data() {
    return {
      startDate: "",
      endDate: "",
      loading: false,
      tables: {
        mainTable: {
          data: [],
          columns: [
            {
              prop: "wfMonth",
              label: "月份",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfName",
              label: "姓名",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfType",
              label: "文件类型",
              fixed: true,
              width: 110,
            },
            {
              prop: "receFiles",
              label: "整编数量(卷)",
              fixed: true,
              width: 110,
            },
            {
              prop: "receDoc",
              label: "整编数量(件)",
              fixed: true,
              width: 110,
            },
            {
              prop: "storeFiles",
              label: "入库数量(卷)",
              fixed: true,
              width: 110,
            },
            {
              prop: "storeDoc",
              label: "入库数量(件)",
              fixed: true,
              width: 110,
            },
          ],
        },
        sonTable: {
          data: [],
          columns: [
            {
              prop: "wfTypeQuarter",
              label: "文件类型",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfFileQT",
              label: "本年本季度(卷)",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfDocQT",
              label: "本年本季度(件)",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfFileQL",
              label: "上年度本季度(卷)",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfDocQL",
              label: "上年度本季度(件)",
              fixed: true,
              width: 110,
            },
            {
              prop: "gRateFile",
              label: "增长数(卷)",
              fixed: true,
              width: 110,
            },
            {
              prop: "gRateDoc",
              label: "增长数(件)",
              fixed: true,
              width: 110,
            },
          ],
        },
      },

      // 本地存储高度名称
      topStorageName: "PreArchiveTopHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 25,
      // 底部除列表高度
      bottomHeight: 35,
      innerTableHeight: window.innerHeight - 360,
    };
  },

  created() {
    this.topPercent = this.getStorageNumber(this.topStorageName, 60);
  },

  methods: {
    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
    },

    handleReport() {
      let _self = this;
      _self.loading = true;

      var m = new Map();
      m.set("startDate", _self.startDate);
      m.set("endDate", _self.endDate);

      axios
        .post("/dms/record/workMonthStatistic", JSON.stringify(m))
        .then(function (response) {
          _self.tables.mainTable.data = response.data.data;
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    handleReportQuarter(){
      let _self = this;
      _self.loading = true;

      var m = new Map();
      m.set("startDate", _self.startDate);
      m.set("endDate", _self.endDate);

      axios
        .post("/dms/record/workQuarterStatistic", JSON.stringify(m))
        .then(function (response) {
          _self.tables.sonTable.data = response.data.data;
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
  },

  components: {
    DataLayout: DataLayout,
  },
};
</script>
<style scoped>
</style>