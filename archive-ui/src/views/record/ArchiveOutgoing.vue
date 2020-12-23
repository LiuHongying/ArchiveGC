<template>
  <DataLayout>
    <template v-slot:header style="height: auto"></template>
    <template v-slot:main>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="待出库" name="ArchivePendingOut">
          <ArchivePendingOut v-if="isArchivePendingOut"></ArchivePendingOut>
        </el-tab-pane>
        <el-tab-pane label="待入库" name="ArchivePendingIn">
          <ArchivePendingIn v-if="isArchivePendingIn"></ArchivePendingIn>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="ArchiveBorrowcompleted">
          <ArchiveBorrowcompleted v-if="isArchiveBorrowcompleted"></ArchiveBorrowcompleted>
        </el-tab-pane>
      </el-tabs>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import DataLayout from "@/components/ecm-data-layout";
import ArchivePendingOut from "@/views/record/ArchivePendingOut.vue";
import ArchivePendingIn from "@/views/record/ArchivePendingIn.vue"
import ArchiveBorrowcompleted from "@/views/record/ArchiveBorrowcompleted.vue"

export default {
  name: "archiveoutgoing",
  data() {
    return {
      activeName:"ArchivePendingOut",
      isArchivePendingOut:true,
      isArchivePendingIn:false,
      isArchiveBorrowcompleted:false,
    }  
  },
  mounted(){
    
  },
  methods: {
    handleClick(tab) {
      if(tab.name == "ArchivePendingOut") {
        this.isArchivePendingOut=true;
        this.isArchivePendingIn=false;
        this.isArchiveBorrowcompleted=false;
      } else if(tab.name == "ArchivePendingIn") {
        this.isArchivePendingOut=false;
        this.isArchivePendingIn=true;
        this.isArchiveBorrowcompleted=false;
      }else if(tab.name == "ArchiveBorrowcompleted") {
        this.isArchivePendingOut=false;
        this.isArchivePendingIn=false;
        this.isArchiveBorrowcompleted=true;
      }
    }
  },
  components: {
    DataLayout: DataLayout,
    ArchivePendingOut:ArchivePendingOut,
    ArchivePendingIn:ArchivePendingIn,
    ArchiveBorrowcompleted:ArchiveBorrowcompleted,
  },
};
</script>
<style scoped>
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
body,html{
			height:100%;
			margin:0px;
			padding:0px;
			overflow:hidden;
		}
    .left,.right{
			width:100%;
      }
      .middle{
        width:5%;
      }
		.left,.middle,.right{
			/* width:200px; */
			/* height:100px; */
			/* background-color:rgb(34,124,134); */
			float:left;
			height:100%;
    }
</style>