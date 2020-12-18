<template>
    <h1>Chemical Search</h1>
    
    <section>
      <input
        class="search-box"
        placeholder="Enter SMILES"
        autofocus
        v-model="todoText"
        @keyup.enter="search"
      />
      <button @click="search">Search</button>
    </section>

    <section>
      <h3>Results</h3>
       <table class="table table-striped table-bordered">
            <thead>
                <tr>
                    <th>Name</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in items" :key="item.id">
                    <td>{{item.name}}</td>
                </tr>
            </tbody>
        </table>
    </section>

</template>

<script lang="ts">
import { Options, setup, Vue } from 'vue-class-component';
import axios from "axios";

const getData = () => [{name:"John"},{name:"Jane"}];

@Options({
})
export default class Home extends Vue {
  keyword?: string;

  items = setup(() => getData())

  search() {
    console.log('search');
    axios
        .get("https://swapi.dev/api/people/", {
          params: {
            search: this.keyword
          }
        })
        .then(res => {
          // eslint-disable-next-line no-console
          console.log(res.data.results);
          this.items = res.data.results;
        })
        .catch(err => {
          // eslint-disable-next-line no-console
          console.log(err);
        });
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
