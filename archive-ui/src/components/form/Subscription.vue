<template>
  <DataLayout>
    <template v-slot:header>
      <el-dialog
        title="保存当前查询条件"
        :visible.sync="saveDialogVisible"
        width="50%"
      >
        <span>
          <el-row :gutter="10">
            <el-col :span="4">
              <p>条件名称：</p>
            </el-col>
            <el-col :span="20">
              <el-input v-model="ConditionName"></el-input>
            </el-col>
          </el-row>
        </span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="saveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCondition()">保存 </el-button>
        </span>
      </el-dialog>
      <el-form :inline="true">
        <el-form-item>
          <el-select v-model="svalue" >
            <el-option
            v-for="item in dataList" 
            :key="item.Condition"
            :label="item.Name"
            :value="item.Condition"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search1()" icon="el-icon-search"
            >查询</el-button
          >
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveDialogVisible = true"
            >保存当前查询条件</el-button
          >
        </el-form-item>
        <el-form-item>
          <el-form-item>
            <AddCondition v-model="AdvCondition" :inputType="hiddenInput" @sendMsg="search()"></AddCondition>
          </el-form-item>
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
                  ref="scriptionGrid"
                  key="scription"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="MySubscribeGrid"
                  :isInitData="false"
                  condition=""
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  showOptions="查看内容"
                  :isShowChangeList="false"
                  @selectchange="selectThChange"
                  @rowclick="onDataGridRowClick"
                >
                </DataGrid>
            </DataGrid>
          </template>
          <template slot="paneR">
            <DataGrid
              ref="scriptionFileGrid"
              key="scriptionFile"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
              v-bind:isshowOption="true" v-bind:isshowSelection ="true"
              gridViewName="MySubscribeFileGrid"
              :isInitData="false"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="false"
              showOptions="查看内容"
              :isShowChangeList="false"
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
import AddCondition from "@/views/record/AddCondition.vue";
export default {
  name: "Subscription",
  data() {
    return {
      isNotCnpe: true,
      batchDialogVisible: false, //导入对话框可见性
      // 本地存储高度名称
      topStorageName: "SubscriptionTopHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight:35,
      ConditionName: "",
      condition:"",
      advCondition: "",
      hiddenInput: "hidden",
      selectedItems: [],
      selectedCondition: [],
      saveDialogVisible: false,
      svalue:"",
      dataList:[],

    };
  },

  mounted() {
    setTimeout(() => {
      this.topPercent = this.getStorageNumber(this.topStorageName, 60);
    }, 300);
    this.laodSelect()
  },

  methods: {
    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
      //console.log(JSON.stringify(topPercent))
    },
    //单击行
    onDataGridRowClick: function (row) {
      this.parentID=row.ID
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children'and PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ") AND IS_RELEASED=1 AND IS_CURRENT = 1";
      this.$refs.scriptionFileGrid.condition = key1;
      this.$refs.scriptionFileGrid.loadGridInfo();
      this.$refs.scriptionFileGrid.loadGridData();
    },
    search(){
      let _self = this
      let key="(C_ITEM_TYPE='文件' or C_ITEM_TYPE='案卷') and IS_RELEASED =1 AND IS_HIDDEN=0 "
      if (_self.AdvCondition != "" && _self.AdvCondition != undefined) {
        key +=" and "+ _self.AdvCondition 
        _self.AdvCondition=''
      }
      _self.$refs.scriptionGrid.condition=key;
      _self.condition = key;
      _self.$refs.scriptionGrid.currentPage = 1;
      _self.$refs.scriptionFileGrid.itemDataList=[];
      _self.$refs.scriptionGrid.loadGridInfo();
      _self.$refs.scriptionGrid.loadGridData();
    },
    search1(){
      let _self = this
      if (_self.svalue != "" && _self.svalue != undefined) {
        _self.$refs.scriptionGrid.condition=_self.svalue;
        _self.$refs.scriptionGrid.currentPage = 1;
        _self.$refs.scriptionFileGrid.itemDataList=[];
        _self.$refs.scriptionGrid.loadGridInfo();
        _self.$refs.scriptionGrid.loadGridData();
      }
      else{
        _self.$message({
          showClose: true,
          message: "请选择查询条件",
          duration: 2000,
          type: "warning",
        });
      }
    },
    saveCondition() {
      let _self = this;
      var map = new Map();
      map.set("Name",_self.ConditionName);
      map.set("Condition",_self.condition);
      axios
        .post("/condition/save", JSON.stringify(map))
        .then(function (response) {
          let code = response.data.code;
          if(code == 1){
            _self.$message({
              showClose: true,
              message:"保存成功",
              duration: 2000,
              type: "success",
            });
            _self.laodSelect()
            _self.saveDialogVisible=false
          } else {
            _self.$message({
              showClose: true,
              message: "保存失败",
              duration: 2000,
              type: "warning",
            });
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    laodSelect() {
      let _self = this;
      axios
        .get("/condition/load")
        .then(function (response) {
          _self.dataList= response.data.data;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
  },

  components: {
    DataLayout: DataLayout,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
  },
};
</script>
<style scoped>
</style>