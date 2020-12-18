<template>
    <h1>{{ msg }}</h1>
    
    <section>
      <h3>Search</h3>
      <input
        class="search-box"
        placeholder="Enter SMILES"
        autofocus
        v-model="todoText"
        @keyup.enter="search"
      />        
    </section>

    <section>
      <h3>Results</h3>
    </section>

</template>

<script lang="ts">
import { Options, setup, Vue } from 'vue-class-component';

@Options({
})
export default class HelloWorld extends Vue {
 
  search() {
    console.log('search');
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
