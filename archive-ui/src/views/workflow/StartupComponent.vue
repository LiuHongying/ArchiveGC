<template>
  <div>
    <el-dialog
      :title="dialogName+$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
      width="90%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <component
        ref="startup"
        :is="componentName"
        :workflowObj="workflowObj"
        @close="closeDialog()"
        :showUploadFile="false"
        :workflowFileList="selectedFiles"
      ></component>
      <!-- <CommonStartup
        ref="startup"
        :workflowObj="workflowObj"
        @close="closeDialog()"
        :showUploadFile="false"
        :workflowFileList="selectedFiles"
      ></CommonStartup> -->
    </el-dialog>
    <el-dialog
      :title="$t('application.WorkflowSelect')"
      :visible.sync="workflowSelectVisible"
      @close="workflowSelectVisible = false"
      width="90%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <DataGrid
        ref="mainDataGrid"
        key="main"
        data-url="/dc/getWorkflows"
        v-bind:tableHeight="500"
        v-bind:isshowOption="true"
        v-bind:isshowSelection="true"
        gridViewName="WorkflowGrid"
        condition="lastVersion"
        :isshowCustom="false"
        :isEditProperty="false"
        showOptions="查看内容"
        :optionWidth="2"
        isInitData
        :isShowChangeList="false"
        @selectchange="selectChange"
      ></DataGrid>
      <div slot="footer" class="dialog-footer">
        <el-button @click="workflowSelectVisible = false">{{$t('application.cancel')}}</el-button>
        <el-button type="primary" @click="selectFlow()">{{$t('application.ok')}}</el-button>
      </div>
    </el-dialog>
    <el-button @click="workflowSelectVisible=true">
      <slot>{{$t('application.StartUpWorkflow')}}</slot>
    </el-button>
  </div>
</template>
<script type='text/javascript'>
import CommonStartup from "@/views/workflow/CommonStartup.vue";
import DataGrid from "@/components/DataGrid";
export default {
  components: {
    CommonStartup: CommonStartup,
    DataGrid: DataGrid
  },
  data() {
    return {
      dialogName: "",
      propertyVisible: false,
      workflowSelectVisible: false,
      workflowObj: {},
      selectFlows: [],
      componentName:""
    };
  },
  props:{
      selectedFiles:[]
  },
  methods: {
    selectChange(val) {
      this.selectFlows = val;
    },
    selectFlow() {
      if (this.selectFlows.length != 1) {
        this.$message({
          showClose: true,
          message: _self.$t("message.PleaseSelectOneData"),
          duration: 2000,
          type: "warning"
        });
        return;
      }
      this.workflowSelectVisible = false;

      let _self = this;
      _self.workflowObj = _self.selectFlows[0];
      this.getEcmcfgActive(this.workflowObj.ID,"start",function(ecmCfgActivity){
        _self.componentName=ecmCfgActivity.componentName;
        _self.propertyVisible=true;
      });
    },
    closeDialog() {
      this.propertyVisible = false;
    }
  }
};
</script>
<style scoped>
</style>