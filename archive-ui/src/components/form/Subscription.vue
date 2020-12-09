<template>
  <DataLayout>
    <template v-slot:header>
      <el-dialog title="保存当前查询条件" :visible.sync="saveDialogVisible" width="65%">
        <span>
          <el-form :inline="true">
            <el-form-item>
              <el-input></el-input>
            </el-form-item>
          </el-form>
        </span>
        <span slot="footer" class="dialog-footer">
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="saveCondition()" 
          >保存
        </el-button>
      </span>
      </el-dialog>
      <el-form :inline="true">
        <el-form-item>
          <el-input style="width:200px" v-model="inputValueNum" :placeholder="$t('message.inputQuestionLimit')"></el-input>
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
            align="right"
            :placeholder="$t('application.endDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search()">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveDialogVisible = true">保存当前查询条件</el-button>
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
            <DataGrid
              ref="mainDataGrid"
              v-bind="tables.main"
              v-bind:tableHeight="
                ((layout.height - startHeight) * topPercent) / 100 -
                topbarHeight
              "
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              :optionWidth="2"
              :isshowCustom="false"
              :isEditProperty="true"
              showOptions="查看内容"
              :isShowChangeList="false"
              @selectchange="onSelectChange"
              @rowclick="onDataGridRowClick"
            >
            </DataGrid>
          </template>
          <template slot="paneR">
            <DataGrid
              ref="relevantFileDataGrid"
              key="relevantFile"
              v-bind="tables.relevantFileDataGrid"
            >
            </DataGrid>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import DataLayout from "@/components/ecm-data-layout";
import DataGrid from "@/components/DataGrid";
export default {
  name: "Subscription",
  data() {
    return {
      isNotCnpe: true,
      batchDialogVisible: false, //导入对话框可见性
      // 本地存储高度名称
      topStorageName: "SubscriptionTopHeight",
      // 非split pan 控制区域高度
      startHeight: 140,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 80,

      restaurants: [],
      inputValueNum: "",

      gridList: [],
      itemDataList: [],
      itemDataListFull: [],
      selectedItems: [],
      selectedCondition: [],

      tables: {
        main: {
          gridViewName: "GeneralPre",
          dataUrl: "/dc/getDocuments",
          condition: "",
        },
        relevantFileDataGrid: {
          gridViewName: "GeneralPre",
          dataUrl: "/dc/getDocuments",
          condition: "C_ITEM_TYPE='案卷'",
        },
      },

      startDate: "",
      endDate: "",
      saveDialogVisible: false,
    };
  },

  mounted() {
    this.restaurants = this.loadAll();

    setTimeout(() => {
      this.topPercent = this.getStorageNumber(this.topStorageName, 60);
    }, 300);
  },

  methods: {
    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
      //console.log(JSON.stringify(topPercent))
    },

    querySearch(queryString, cb) {
      var restaurants = this.restaurants;
      var results = queryString
        ? restaurants.filter(this.createFilter(queryString))
        : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },

    createFilter(queryString) {
      return (restaurant) => {
        return (
          restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) ===
          0
        );
      };
    },

    loadAll(indata) {
      let _self = this;
    },

    handleSelect(val) {
      this.selectedCondition = val;
    },

    onSelectChange(val) {
      this.selectedItems = val;
    },

    search() {
      let _self = this;

      _self.$refs.tables.main.itemDataList = [];
      _self.$refs.tables.relevantFileDataGrid.itemDataList = [];
      let user = _self.currentUser();

      var conditionNew = " CREATOR = '" + user + "' and ";
      _self.inputValueNum;
    },

    handleIconClick(indata) {
      let _self = this;

      _self.$refs.tables.main.itemDataList = [];
      _self.$refs.tables.relevantFileDataGrid.itemDataList = [];

      var key = indata.Condition;
      _self.$refs.mainDataGrid.condition = key;
      _self.tables.main.condition = key;
      _self.$refs.mainDataGrid.loadGridData();
    },

    saveCondition(indata) {
      let _self = this;
      var map = new Map();
      var user = this.currentUser();

      map.set("loginName", user);
      map.set("Name", indata.Name);

      axios.post();
    },
  },

  components: {
    DataLayout: DataLayout,
    DataGrid: DataGrid,
  },
};
</script>
<style scoped>
</style>