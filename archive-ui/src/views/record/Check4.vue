<template>
  <div>
    <el-container>
      <!--<el-header>
        <el-row>
          <el-col :span="24" class="topbar-button">
            
            <el-date-picker
              style="width:14em"
              type="date"
              v-model="firstTime"
              value-format="yyyy-MM-dd"
              placeholder="开始时间"
            ></el-date-picker>
            <el-date-picker
              style="width:14em"
              type="date"
              v-model="endTime"
              value-format="yyyy-MM-dd"
              placeholder="结束时间"
            ></el-date-picker>
            <el-button type="primary" plain  @click="refreshData" v-loading="buttloading">开始检查</el-button>
            &nbsp;
           <!-- <el-button type="primary" plain  @click="exortExcel">{{$t('application.ExportExcel')}}</el-button>
            <el-button type="primary" @click="searchAll">所有数据</el-button>
            
          </el-col>
        </el-row>
      </el-header>-->
      <el-main>
        <el-table id="outTable" :data="checkData" v-loading="loading" height="500px">
          <el-table-column type="index" width="50" >
          </el-table-column>
          <el-table-column
              prop="CODING"
              label="编码"
              width="150">
            </el-table-column>
            <el-table-column
              prop="REVISION"
              label="版本"
              width="100">
            </el-table-column>
            <el-table-column
              prop="TITLE"
              label="标题"
              width="180">
            </el-table-column>
          <el-table-column label="真实性">
            <el-table-column
              prop="trueSource"
              label="来源"
              width="100">
            </el-table-column>
            <el-table-column
              prop="trueMetaData"
              label="元数据"
              width="100">
            </el-table-column>
            <el-table-column
              prop="trueContent"
              label="内容"
              width="100">
            </el-table-column>
          </el-table-column>
          <el-table-column label="完整性">
            <el-table-column
              prop="integrityMetaData"
              label="元数据"
              width="100">
            </el-table-column>
            <el-table-column
              prop="integrityContent"
              label="内容"
              width="100">
            </el-table-column>
            <el-table-column
              prop="integrityPackage"
              label="信息包"
              width="100">
            </el-table-column>
          </el-table-column>
          <el-table-column label="可用性">
            <el-table-column
              prop="useMetaData"
              label="元数据"
              width="100">
            </el-table-column>
            <el-table-column
              prop="useContent"
              label="内容"
              width="100">
            </el-table-column>
          </el-table-column>
          <el-table-column label="安全性">
            <el-table-column
              prop="securityVirus"
              label="病毒检测"
              width="100">
            </el-table-column>
          </el-table-column>
          <el-table-column label="总体结果" prop="totalResult"></el-table-column>
          </el-table>
      </el-main>
      <el-footer>
      <el-button type="primary" @click="refreshData" :disabled="buttloading==-1?true:false">开始检查</el-button>
      <el-button type="primary" @click="closePage()">{{$t('application.cancel')}}</el-button>
      </el-footer>
    </el-container>
  </div>
</template>

<script type="text/javascript">
import FileSaver from 'file-saver'
import XLSX from 'xlsx'

export default {
  data() {
    return { 
    buttloading:0,
    loading:false,
    // checkData:[],
    classicData: [],
    classic: "",
    firstTime:'',
    endTime:'',
    findType:''
    };
  },
  mounted(){ 
    let _self = this;
    _self.loadClassic();
    var user = sessionStorage.getItem('access-user');
    if(user)
    {
      _self.clientPermission = sessionStorage.getItem('access-clientPermission');
    }
    let date = new Date();
    let startDate =new Date();
    startDate.setTime(date.getTime() - 3600 * 1000 * 24 * 90);
    let startYear = startDate.getFullYear();
    let year = date.getFullYear();
    let startMonth = startDate.getMonth()+1;
    let startDay = startDate.getDate()
    let month = date.getMonth()+1;
    let day = date.getDate();
    _self.firstTime = startYear+"-"+startMonth+"-"+startDay
    _self.endTime = year+"-"+month+"-"+day
    //_self.refreshData();
  },
  props:{
    checkData:{type:Array,default:[]}
  },
  methods: {
    loadClassic() {
      let _self = this;
      _self.loading = true
      axios.post('/record/getArchiveClassic')
      .then(function(response) {
        _self.classicData = response.data.data
        _self.loading = false;
      })
      .catch(function(error) {
        console.log(error);
      });
    },
    exortExcel(){
       var wb = XLSX.utils.table_to_book(document.querySelector('#outTable'))
         /* get binary string as output */
         var wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: true, type: 'array' })
         try {
             FileSaver.saveAs(new Blob([wbout], { type: 'application/octet-stream' }), 'docCheckData.xlsx')
         } catch (e) { if (typeof console !== 'undefined') console.log(e, wbout) }
         return wbout
    },
    refreshData() {
      let _self = this;
      _self.loading = true
      _self.buttloading=-1
      var m = [];
      let tab = _self.checkData;
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      axios
      .post("/record/getCheck4Data",JSON.stringify(m),{
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        }
      })
      .then(function(response) {
        let code = response.data.code;
        if (code == 1) {
          _self.checkData = response.data.data
          _self.loading = false;
          _self.buttloading=0;
            _self.$message({
            showClose: true,
            message: '检查结束',
            type: 'success',
            duration:2000
          });
        }
      })
      .catch(function(error) {
        console.log(error);
      });
    },
    closePage(pv){
        this.$emit("close");
        this.buttloading=0;
    },
  },
};
</script>


<style scoped>
.el-header{
  background-color: #E9EEF3;
  text-align: left;
}
</style>
