<template>
  <DataLayout>
    <template v-slot:header style="height: auto">
      <el-form :inline="true">
        <el-form-item>
          <el-date-picker
            v-model="yearS"
            type="year"
            placeholder="选择年份"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-select v-model="value" placeholder="请选择季度">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleReport()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <el-table
        ref="mainTable"
        :data="tables.mainTable.data"
        :span-method="objectSpanMethod"
        border
        stripe
        size="mini"
        v-loading="loading"
        :height="layout.height"
      >
        <el-table-column type="index" width="30" fixed></el-table-column>
        <el-table-column width="120" prop="date"></el-table-column>
        <template v-for="item in tables.mainTable.columns">
          <el-table-column
            v-if="item.displayNum == '0'"
            :key="item.prop"
            v-bind="item"
          >
          </el-table-column>
          <el-table-column
            v-else-if="item.displayNum == value"
            :key="item.prop"
            v-bind="item"
          >
          </el-table-column>
        </template>
      </el-table>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "fileUpdateWorkFlow",
  data() {
    return {
      spanArr: [],
      yearS: "",

      tables: {
        mainTable: {
          data: [],
          columns: [
            {
              prop: "fileType",
              label: "文件类型",
              displayNum: "0",
              fixed: true,
              width: 110,
            },
            {
              prop: "typeClass",
              label: "类别",
              displayNum: "0",
              fixed: true,
              width: 110,
            },
            {
              prop: "unitType",
              label: "单位",
              displayNum: "0",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth1",
              label: "一月",
              displayNum: "1",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth2",
              label: "二月",
              displayNum: "1",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth3",
              label: "三月",
              displayNum: "1",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitQuarter",
              label: "第一季度",
              displayNum: "1",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth1",
              label: "四月",
              displayNum: "2",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth2",
              label: "五月",
              displayNum: "2",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth3",
              label: "六月",
              displayNum: "2",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitQuarter",
              label: "第二季度",
              displayNum: "2",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth1",
              label: "七月",
              displayNum: "3",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth2",
              label: "八月",
              displayNum: "3",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth3",
              label: "九月",
              displayNum: "3",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitQuarter",
              label: "第三季度",
              displayNum: "3",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth1",
              label: "十月",
              displayNum: "4",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth2",
              label: "十一月",
              displayNum: "4",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitMonth3",
              label: "十二月",
              displayNum: "4",
              fixed: true,
              width: 110,
            },
            {
              prop: "drawCountSuitQuarter",
              label: "第四季度",
              displayNum: "4",
              fixed: true,
              width: 110,
            },
          ],
        },
      },

      options: [
        {
          value: "1",
          label: "第一季度",
        },
        {
          value: "2",
          label: "第二季度",
        },
        {
          value: "3",
          label: "第三季度",
        },
        {
          value: "4",
          label: "第四季度",
        },
      ],
      value: "",
    };
  },

  components: {
    DataLayout: DataLayout,
  },

  methods: {
    handleReport() {
      let _self = this;
      _self.loading = true;

      var m = new Map();
      m.set("yearSelect", _self.yearS);
      m.set("quarterSelect", _self.value);

      axios
        .post("/dms/record/fileUpVersionWorkStatistic", JSON.stringify(m))
        .then(function (response) {
          _self.tables.mainTable.data = response.data.data;
          console.log(_self.tables.mainTable.data);
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
  },
};
</script>
<style scoped>
</style>