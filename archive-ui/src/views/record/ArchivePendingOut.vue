<template>
    <DataLayout>
    <template v-slot:header>
      <el-form :inline="true">
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编号或说明'
            @keyup.enter.native="search()"
          ></el-input>
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
          <el-button type="primary" @click="search()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit('主表')">出库</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
          <template slot="paneL">
                <DataGrid
                  ref="APendingoutGrid"
                  key="APendingout"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="FormGrid"
                  condition="STATUS='待出库'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  :isShowMoreOption="false"
                  :isshowicon="false"
                  :isShowChangeList="false"
                  :isInitData="false"
                  @rowclick="onDataGridRowClick"
                  @selectchange="selectChange"
                >
                </DataGrid>
          </template>
          <template slot="paneR">
            <el-tabs v-model="activeName">
              <el-tab-pane label="待出库" name="ArchivePendingOut">
                <el-form :inline="true">
                  <el-form-item>
                    <el-input
                      style="width: 200px"
                      v-model="inputdcing"
                      placeholder='请输入编号'
                      @keyup.enter.native="searchDCing()"
                    ></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="searchDCing()">{{
                      $t("application.SearchData")
                    }}</el-button>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submit('')">出库</el-button>
                  </el-form-item>
                </el-form>
                <DataGrid
                  ref="PendingoutGrid"
                  key="Pendingout"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="FormDcGrid"
                  condition="STATUS='待出库'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  showOptions="查看内容"
                  isShowChangeList="false"
                  :isInitData="false"
                  @selectchange="selectDCChange"
                >
                </DataGrid>
              </el-tab-pane>
              <el-tab-pane label="已出库" name="ArchivePending">
                <el-form :inline="true">
                  <el-form-item>
                    <el-input
                      style="width: 200px"
                      v-model="inputdced"
                      placeholder='请输入编码'
                      @keyup.enter.native="searchDCed()"
                    ></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="searchDCed()">{{
                      $t("application.SearchData")
                    }}</el-button>
                  </el-form-item>
                </el-form>
                <DataGrid
                  ref="PendedoutGrid"
                  key="Pendedout"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="FormDcGrid"
                  condition="STATUS='已出库'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  showOptions="查看内容"
                  isShowChangeList="false"
                  :isInitData="false"
                >
                </DataGrid>
              </el-tab-pane>
            </el-tabs>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import DataGrid from "@/components/DataGrid";
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "TC",
  data() {
    return {
      // 本地存储高度名称
      topStorageName: 'ReceivingDC4CnpeHeight',
      // 非split pan 控制区域高度
      startHeight: 180,
      // 顶部百分比*100
      topPercent: 55,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight:120,
      inputValueNum:"",
      startDate: "",
      endDate:"",
      activeName:"ArchivePendingOut",
      typename:"'借阅单'",
      parentId:"",
      condition1:"",
      condition2:"",
      inputdcing:"",
      inputdced:"",
      selectedDCItems: [],//文档
      selectedItems:[],//出入库
    };
  },
  mounted() {
    if (!this.validataPermission()) {
      //跳转至权限提醒页
      let _self = this;
      _self.$nextTick(() => {
        _self.$router.push({ path: "/NoPermission" });
      });
    }
    this.search();
  },
  methods: {
    // 上下分屏事件
    onSplitResize(topPercent){
      // 顶部百分比*100
      this.topPercent = topPercent
      this.setStorageNumber(this.topStorageName, topPercent)
      //console.log(JSON.stringify(topPercent))
    },
    //单击行
    onDataGridRowClick: function (row) {
      this.parentId=row.ID
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ") AND STATUS='待出库'";
      this.condition1=key1
      this.$refs.PendingoutGrid.condition = key1;
      this.$refs.PendingoutGrid.loadGridInfo();
      this.$refs.PendingoutGrid.loadGridData();
      var key2 = "ID IN (" + condition1 + ") AND STATUS='待入库'"
      this.condition2=key2
      this.$refs.PendedoutGrid.condition = key2;
      this.$refs.PendedoutGrid.loadGridInfo();
      this.$refs.PendedoutGrid.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME IN("+_self.typename+") AND STATUS='待出库'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR C_COMMENT LIKE '%"+_self.inputValueNum+"%')";
      }
      if(_self.startDate!=''&&_self.startDate!=undefined){
          key+=" and CREATION_DATE > '"+_self.startDate+"'";
      }
      if(_self.endDate!=''&&_self.endDate!=undefined){
          key+=" and CREATION_DATE < '"+_self.endDate+"'";
      }
      _self.$refs.APendingoutGrid.condition=key;
      _self.$refs.APendingoutGrid.currentPage = 1;
      _self.$refs.APendingoutGrid.loadGridInfo();
      _self.$refs.APendingoutGrid.loadGridData();
    },
    searchDCing() {
      let _self = this;
      let key = _self.condition1
      if(_self.inputdcing!=''&&_self.inputdcing!=undefined){
        key+=" and (CODING LIKE '%"+_self.inputdcing+"%')";
      }
      _self.$refs.PendingoutGrid.condition=key;
      _self.$refs.PendingoutGrid.currentPage = 1;
      _self.$refs.PendingoutGrid.loadGridInfo();
      _self.$refs.PendingoutGrid.loadGridData();
    },
    searchDCed() {
      let _self = this;
      let key = _self.condition2
      if(_self.inputdced!=''&&_self.inputdced!=undefined){
        key+=" and (CODING LIKE '%"+_self.inputdced+"%')";
      }
      _self.$refs.PendedoutGrid.condition=key;
      _self.$refs.PendedoutGrid.currentPage = 1;
      _self.$refs.PendedoutGrid.loadGridInfo();
      _self.$refs.PendedoutGrid.loadGridData();
    },
    selectDCChange(val) {
      this.selectedDCItems = val;
    },
    selectChange(val) {
      this.selectedItems = val;
    },
    submit(type){
      let _self =this
      var m = new Map();
      m.set("type",type)
      m.set("status","待入库")
      m.set("parentID",_self.parentId)
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));
      var a = [];
      let tab;
      if(type=="主表"){
        tab = _self.selectedItems;
      }else{
        tab = _self.selectedDCItems;
      }
      var i;
      for (i in tab) {a
        a.push(tab[i]["ID"]);
      }
      formdata.append("ID", JSON.stringify(a));
      axios
      .post("/dc/Archivepending", formdata, {
        "Content-Type": "multipart/form-data",
      })
      .then(function (response) {
        let code = response.data.code;
        let al = response.data.al;
        if (code == 1) {
          _self.$message({
            showClose: true,
            message: "出库成功",
            duration: 2000,
            type: "success",
          });
          if(type=="主表"){
            _self.search();
            _self.$refs.PendingoutGrid.itemDataList=[];
            _self.$refs.PendedoutGrid.itemDataList=[];
          }else{
            _self.searchDCing()
            _self.searchDCed()
          }
          if(al){
            _self.search();
          }
        } else {
          _self.$message({
            showClose: true,
            message:"出库失败",
            duration: 2000,
            type: "warning",
          });
        }
      })
      .catch(function (error) {
        _self.$message("出库失败");
        console.log(error);
      });
    }
  },
  props: {},
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
  },
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 0px;
}
.el-table td,
.el-table th {
  text-align: center !important;
}
</style>