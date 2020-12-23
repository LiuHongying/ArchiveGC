<template>
    <DataLayout>
    <template v-slot:header>
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编码或说明'
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
          <el-button type="primary" @click="submit('主表')">入库</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
          <template slot="paneL">
                <DataGrid
                  ref="APendingGrid"
                  key="APending"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="FormGrid"
                  condition="STATUS='待入库'"
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
              <el-tab-pane label="待入库" name="ArchivePendingOut">
                <el-form :inline="true" @submit.native.prevent>
                  <el-form-item>
                    <el-input
                      style="width: 200px"
                      v-model="inputdcing"
                      placeholder='请输入编码'
                      @keyup.enter.native="searchDCing()"
                    ></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="searchDCing()">{{
                      $t("application.SearchData")
                    }}</el-button>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submit('')">入库</el-button>
                  </el-form-item>
                </el-form>
                <DataGrid
                  ref="PendingGrid"
                  key="PendingOut"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="FormDcGrid"
                  condition="STATUS='待入库'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  showOptions="查看内容"
                  :isShowChangeList="false"
                  :isInitData="false"
                  @selectchange="selectDCChange"
                >
                </DataGrid>
              </el-tab-pane>
              <el-tab-pane label="已入库" name="ArchivePending">
                <el-form :inline="true" @submit.native.prevent>
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
                  ref="PendedGrid"
                  key="Pended"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="FormDcGrid"
                  condition="STATUS='已完成'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  showOptions="查看内容"
                  :isShowChangeList="false"
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
      typename:"'借阅单','复制单','设计文件修改单','科研文件借阅单','科研文件修改单'",
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
      var key1 = "ID IN (" + condition1 + ") AND STATUS='待入库'";
      this.condition1=key1
      this.$refs.PendingGrid.condition = key1;
      this.$refs.PendingGrid.loadGridInfo();
      this.$refs.PendingGrid.loadGridData();
      var key2 = "ID IN (" + condition1 + ") AND STATUS='已完成'";
      this.condition2=key2
      this.$refs.PendedGrid.condition = key2;
      this.$refs.PendedGrid.loadGridInfo();
      this.$refs.PendedGrid.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME IN("+_self.typename+") AND STATUS='待入库'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR C_COMMENT LIKE '%"+_self.inputValueNum+"%')";
      }
      if(_self.startDate!=''&&_self.startDate!=undefined){
          key+=" and CREATION_DATE > '"+_self.startDate+"'";
      }
      if(_self.endDate!=''&&_self.endDate!=undefined){
          key+=" and CREATION_DATE < '"+_self.endDate+"'";
      }
      _self.$refs.APendingGrid.condition=key;
      _self.$refs.APendingGrid.currentPage = 1;
      _self.$refs.APendingGrid.loadGridInfo();
      _self.$refs.APendingGrid.loadGridData();
    },
    searchDCing() {
      let _self = this;
      if(_self.parentId==""||_self.parentId==undefined){
        _self.$message({
            showClose: true,
            message: "请选择一条文件",
            duration: 2000,
            type: "warning",
          });
          return
      }
      let key = _self.condition1
      if(_self.inputdcing!=''&&_self.inputdcing!=undefined){
        key+=" and (CODING LIKE '%"+_self.inputdcing+"%')";
      }
      _self.$refs.PendingGrid.condition=key;
      _self.$refs.PendingGrid.currentPage = 1;
      _self.$refs.PendingGrid.loadGridInfo();
      _self.$refs.PendingGrid.loadGridData();
    },
    searchDCed() {
      let _self = this;
      if(_self.parentId==""||_self.parentId==undefined){
        _self.$message({
            showClose: true,
            message: "请选择一条文件",
            duration: 2000,
            type: "warning",
          });
          return
      }
      let key = _self.condition2
      if(_self.inputdced!=''&&_self.inputdced!=undefined){
        key+=" and (CODING LIKE '%"+_self.inputdced+"%')";
      }
      _self.$refs.PendedGrid.condition=key;
      _self.$refs.PendedGrid.currentPage = 1;
      _self.$refs.PendedGrid.loadGridInfo();
      _self.$refs.PendedGrid.loadGridData();
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
      m.set("status","已完成")
      m.set("parentID",_self.parentId)
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));
      var a = [];
      let tab;
      if(type=="主表"){
        if(_self.selectedItems==''||_self.selectedItems==undefined){
           _self.$message({
            showClose: true,
            message: "请选择一条文件",
            duration: 2000,
            type: "warning",
          });
          return
        }
        tab = _self.selectedItems;
      }else{
        if(_self.selectedDCItems==''||_self.selectedDCItems==undefined){
           _self.$message({
            showClose: true,
            message: "请选择一条文件",
            duration: 2000,
            type: "warning",
          });
          return
        }
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
            message: "入库成功",
            duration: 2000,
            type: "success",
          });
          if(type=="主表"){
            _self.search();
            _self.$refs.PendingGrid.itemDataList=[];
            _self.$refs.PendedGrid.itemDataList=[];
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
            message:"入库失败",
            duration: 2000,
            type: "warning",
          });
        }
      })
      .catch(function (error) {
        _self.$message("入库失败");
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