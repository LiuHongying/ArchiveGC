<template>
  <DataLayout>
    <el-tabs value="t01">
      <el-tab-pane label="待阅" name="t01">
          <DistributeList></DistributeList>
      </el-tab-pane>
      <el-tab-pane label="已阅" name="t02">
          <DistributeListReaded></DistributeListReaded>
      </el-tab-pane>
      <el-tab-pane label="我的分发" name="t03">
          <DistributeListSender></DistributeListSender>
      </el-tab-pane>
      <el-tab-pane label="批示" name="t04">
          <DistributeApproval></DistributeApproval>
      </el-tab-pane>
    </el-tabs>
  </DataLayout>
</template>
<script>
import DistributeList from "@/views/archivecd/distribute/DistributeList.vue";
import DistributeListReaded from "@/views/archivecd/distribute/DistributeListReaded.vue";
import DistributeListSender from "@/views/archivecd/distribute/DistributeListSender.vue";
import DistributeApproval from "@/views/archivecd/distribute/DistributeApproval.vue";
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "DistributeManagement",
  components: {
    DistributeApproval: DistributeApproval,
    DistributeList: DistributeList,
    DistributeListReaded: DistributeListReaded,
    DistributeListSender: DistributeListSender,
    DataLayout: DataLayout
  },
  data() {
    return {};
  },
  props: {},
  mounted() {},
  methods: {}
};
</script>
<style scoped>
</style>


